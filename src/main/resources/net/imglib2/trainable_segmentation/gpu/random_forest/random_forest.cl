__constant sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

#define PIXEL(image, x, y, z) image[((z) * GET_IMAGE_HEIGHT(image) + (y)) * GET_IMAGE_WIDTH(image) + (x)]
#define IMAGE_TYPE(a) IMAGE_ ## a ## _TYPE a
#define GET_IMAGE_VOLUME(image) (GET_IMAGE_WIDTH(image) * GET_IMAGE_HEIGHT(image) * GET_IMAGE_DEPTH(image))

__kernel void random_forest
(
  IMAGE_TYPE(dst),
  IMAGE_TYPE(src),
  CONSTANT_OR_GLOBAL float *thresholds,
  __global float *probabilities,
  CONSTANT_OR_GLOBAL short *indices
)
{
  const int x = get_global_id(0), y = get_global_id(1), z = get_global_id(2);
  const int src_channel_skip = GET_IMAGE_VOLUME(src);
  const int dst_channel_skip = GET_IMAGE_VOLUME(dst);
  const int num_trees = GET_IMAGE_DEPTH(thresholds);
  const unsigned short num_nodes = (unsigned short) GET_IMAGE_HEIGHT(thresholds);
  float results[NUMBER_OF_CLASSES];
  float features[NUMBER_OF_FEATURES];

  // zero probabilities
  for(int i = 0; i < NUMBER_OF_CLASSES; i++) {
    results[i] = 0;
  }

  for(int i = 0; i < NUMBER_OF_FEATURES; i++) {
    features[i] = PIXEL(src, x + i * src_channel_skip, y, z);
  }

  // run random forest
  for(int tree = 0; tree < num_trees; tree++) {
    unsigned short nodeIndex = 0;
    while(nodeIndex < num_nodes) {
      const unsigned short attributeIndex = PIXEL(indices, 0, nodeIndex, tree);
      const float attributeValue = features[attributeIndex];
      const float threshold = PIXEL(thresholds, 0, nodeIndex, tree);
      const int smaller = (int) (attributeValue >= threshold) + 1;
      nodeIndex = PIXEL(indices, smaller, nodeIndex, tree);
    }
    const unsigned short leafIndex = nodeIndex - num_nodes;
    for(int i = 0; i < NUMBER_OF_CLASSES; i++) {
      results[i] += probabilities[(tree * GET_IMAGE_HEIGHT(probabilities) + leafIndex) * NUMBER_OF_CLASSES + i];
    }
  }

  // calculate sum of the distribution
  float sum = 0;
  for(int i = 0; i < NUMBER_OF_CLASSES; i++) {
    sum += results[i];
  }

  // normalize distribution
  for(int i = 0; i < NUMBER_OF_CLASSES; i++) {
    PIXEL(dst, x + i * dst_channel_skip, y, z) = results[i] / sum;
  }
}
