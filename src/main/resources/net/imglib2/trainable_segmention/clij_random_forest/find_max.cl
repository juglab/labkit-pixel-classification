__constant sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__kernel void find_max
(
  IMAGE_dst_TYPE dst,
  IMAGE_src_TYPE src,
  const int num_classes
)
{
  const int x = get_global_id(0), y = get_global_id(1), z = get_global_id(2);
  const int num_slices = GET_IMAGE_DEPTH(src) / num_classes;

  // normalize distribution
  int maxIndex = 0;
  int4 pos = (int4)(x,y,z,0);
  float maxValue = READ_src_IMAGE(src, sampler, pos).x;
  for(int i = 1; i < num_classes; i++) {
    pos.z = z + i * num_slices;
    float value = READ_src_IMAGE(src, sampler, pos).x;
    bool bigger = value > maxValue;
    maxValue = bigger ? value : maxValue;
    maxIndex = bigger ? i : maxIndex;
  }
  WRITE_dst_IMAGE(dst, (int4)(x,y,z,0), maxIndex);
}
