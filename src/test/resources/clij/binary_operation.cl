const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

#define PIXEL(a) (a[index])
#define CALCULATE_INDEX const int x = get_global_id(0); const int y = get_global_id(1); const int z = get_global_id(2); const int index = (z * GET_IMAGE_HEIGHT(a) + y) * GET_IMAGE_WIDTH(a) + x;

#ifdef OPERATION_1
__kernel void operation_1(IMAGE_a_TYPE a) {
  CALCULATE_INDEX;
  OPERATION_1(PIXEL(a));
}
#endif

#ifdef OPERATION_2
__kernel void operation_2(IMAGE_a_TYPE a, IMAGE_b_TYPE b) {
  CALCULATE_INDEX;
  OPERATION_2(PIXEL(a), PIXEL(b));
}
#endif

#ifdef OPERATION_3
__kernel void operation_3(IMAGE_a_TYPE a, IMAGE_b_TYPE b, IMAGE_c_TYPE c) {
  CALCULATE_INDEX;
  OPERATION_3(PIXEL(a), PIXEL(b), PIXEL(c));
}
#endif


