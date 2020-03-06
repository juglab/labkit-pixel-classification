const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

#define PIXEL(a) (a[index])
#define CALCULATE_INDEX const int x = get_global_id(0); const int y = get_global_id(1); const int z = get_global_id(2); const int index = (z * GET_IMAGE_HEIGHT(a) + y) * GET_IMAGE_WIDTH(a) + x;

__kernel void operation(PARAMETER) {
  CALCULATE_INDEX;
  OPERATION;
}
