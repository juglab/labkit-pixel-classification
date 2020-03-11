const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

#define CALCULATE_INDEX(image, x, y, z) (((z) * GET_IMAGE_HEIGHT(image) + (y)) * GET_IMAGE_WIDTH(image) + (x))
#define PIXEL_AT(image, x, y, z) (image[CALCULATE_INDEX(image, x, y, z)])
#define PIXEL_OFFSET(image, ox, oy, oz) PIXEL_AT(image, coordinate_x + ox, coordinate_y + oy, coordinate_z + oz)
#define PIXEL(image) PIXEL_AT(image, coordinate_x, coordinate_y, coordinate_z)

__kernel void operation(PARAMETER) {
  const int coordinate_x = get_global_id(0);
  const int coordinate_y = get_global_id(1);
  const int coordinate_z = get_global_id(2);
  OPERATION;
}
