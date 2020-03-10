const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

#define CALCULATE_INDEX(image, x, y, z) (((z) * GET_IMAGE_HEIGHT(image) + (y)) * GET_IMAGE_WIDTH(image) + (x))
#define PIXEL_AT(image, x, y, z) (image[CALCULATE_INDEX(image, x, y, z)])
#define PIXEL(image) PIXEL_AT(image, x, y, z)

__kernel void operation(PARAMETER) {
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);
  OPERATION;
}
