__constant sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

#define READ_IMAGE(a,b,c) READ_ ## a ## _IMAGE(a,b,c)
#define WRITE_IMAGE(a,b,c) WRITE_ ## a ## _IMAGE(a,b,c)
#define IMAGE_TYPE(a) IMAGE_ ## a ## _TYPE a

__kernel void random_forest
(
  IMAGE_dst_TYPE dst,
  IMAGE_src_TYPE src,
  IMAGE_thresholds_TYPE thresholds,
  IMAGE_probabilities_TYPE probabilities,
  IMAGE_indices_TYPE indices,
  const int num_features
)
{
  const int x = get_global_id(0), y = get_global_id(1), z = get_global_id(2);
  const int offsetInput = z * num_features;
  const int num_classes = GET_IMAGE_WIDTH(probabilities);
  const int offsetOutput = z * num_classes;
  const int num_trees = GET_IMAGE_DEPTH(thresholds);
  const unsigned short num_nodes = (unsigned short) GET_IMAGE_HEIGHT(thresholds);

  // zero probabilities
  for(int i = 0; i < num_classes; i++) {
    const int4 pos = (int4)(x,y,i + offsetOutput,0);
    WRITE_dst_IMAGE(dst, pos, 0);
  }

  // run random forest
  for(int tree = 0; tree < num_trees; tree++) {
    unsigned short nodeIndex = 0;
    while(nodeIndex < num_nodes) {
      const unsigned short attributeIndex = READ_IMAGE(indices, sampler, (int4)(0,nodeIndex,tree,0)).x;
      const float attributeValue = READ_IMAGE(src, sampler, (int4)(x,y,attributeIndex + offsetInput,0)).x;
      const float threshold = READ_IMAGE(thresholds, sampler, (int4)(0,nodeIndex,tree,0)).x;
      const int smaller = (int) (attributeValue >= threshold) + 1;
      nodeIndex = READ_IMAGE(indices, sampler, (int4)(smaller,nodeIndex,tree,0)).x;
    }
    const unsigned short leafIndex = nodeIndex - num_nodes;
    for(int i = 0; i < num_classes; i++) {
      const int4 pos = (int4)(x,y,i + offsetOutput,0);
      float probability = READ_IMAGE(dst, sampler, pos).x;
      probability += READ_IMAGE(probabilities, sampler, (int4)(i,leafIndex,tree,0)).x;
      WRITE_IMAGE(dst, pos, probability);
    }
  }

  // calculate sum of the distribution
  float sum = 0;
  for(int i = 0; i < num_classes; i++) {
    sum += READ_IMAGE(dst, sampler, (int4)(x,y,i + offsetOutput,0)).x;
  }

  // normalize distribution
  for(int i = 0; i < num_classes; i++) {
    const int4 pos = (int4)(x,y,i + offsetOutput,0);
    float probability = READ_IMAGE(dst, sampler, pos).x;
    probability /= sum;
    WRITE_IMAGE(dst, pos, probability);
  }
}
