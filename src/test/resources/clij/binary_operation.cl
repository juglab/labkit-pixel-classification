const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

#define PIXEL(a) (a[index])

__kernel void binary_operation(IMAGE_a_TYPE a, IMAGE_b_TYPE b, IMAGE_c_TYPE c) {
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);
  const int index = (z * GET_IMAGE_HEIGHT(a) + y) * GET_IMAGE_WIDTH(a) + x;
  OPERATION(PIXEL(a), PIXEL(b), PIXEL(c));
}
