#define PIXEL(image, x, y, z) (image[((z) * GET_IMAGE_HEIGHT(image) + (y)) * GET_IMAGE_WIDTH(image) + (x)])

__kernel void find_max
(
  IMAGE_dst_TYPE dst,
  IMAGE_src_TYPE src,
  const int num_classes
)
{
  const int x = get_global_id(0), y = get_global_id(1), z = get_global_id(2);
  const int src_channel_skip = GET_IMAGE_WIDTH(src) * GET_IMAGE_HEIGHT(src) * GET_IMAGE_DEPTH(src);

  int maxIndex = 0;
  float maxValue = PIXEL(src, x, y, z);
  for(int i = 1; i < num_classes; i++) {
    float value = PIXEL(src, x + i * src_channel_skip, y, z);
    bool bigger = value > maxValue;
    maxValue = bigger ? value : maxValue;
    maxIndex = bigger ? i : maxIndex;
  }
  PIXEL(dst, x, y, z) = maxIndex;
}
