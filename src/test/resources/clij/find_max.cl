__constant sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__kernel void find_max
(
  __global float* dst,
  __global float* src,
  const int num_classes
)
{
  const int x = get_global_id(0), y = get_global_id(1), z = get_global_id(2);
  const int offset = z * num_classes;

  // normalize distribution
  int maxIndex = 0;
  int4 pos = (int4)(x,y,offset,0);
  float maxValue = READ_IMAGE_3D(src, sampler, pos).x;
  for(int i = 1; i < num_classes; i++) {
    pos.z = i + offset;
    float value = READ_IMAGE_3D(src, sampler, pos).x;
    bool bigger = value > maxValue;
    maxValue = bigger ? value : maxValue;
    maxIndex = bigger ? i : maxIndex;
  }
  WRITE_IMAGE_3D(dst, (int4)(x,y,z,0), maxIndex);
}
