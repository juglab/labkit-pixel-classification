__kernel void copy_with_offset(IMAGE_dst_TYPE dst, IMAGE_src_TYPE src, int src_offset_x, int src_offset_y, int src_offset_z, int dst_offset_x, int dst_offset_y, int dst_offset_z) {
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

  const int sx = get_global_id(0) + src_offset_x;
  const int sy = get_global_id(1) + src_offset_y;
  const int sz = get_global_id(2) + src_offset_z;
  POS_src_TYPE src_pos = POS_dst_INSTANCE(sx, sy, sz, 0);

  const int dx = get_global_id(0) + dst_offset_x;
  const int dy = get_global_id(1) + dst_offset_y;
  const int dz = get_global_id(2) + dst_offset_z;
  POS_dst_TYPE dst_pos = POS_dst_INSTANCE(dx, dy, dz, 0);

  const IMAGE_src_PIXEL_TYPE value = READ_src_IMAGE(src,sampler, src_pos).x;
  WRITE_dst_IMAGE(dst, dst_pos, (IMAGE_dst_PIXEL_TYPE) value);
}
