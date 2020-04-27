__kernel void copy_with_offset(DTYPE_IMAGE_OUT_3D dst, DTYPE_IMAGE_IN_3D src, int src_offset_x, int src_offset_y, int src_offset_z, int dst_offset_x, int dst_offset_y, int dst_offset_z) {
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

  const int dx = get_global_id(0) + dst_offset_x;
  const int dy = get_global_id(1) + dst_offset_y;
  const int dz = get_global_id(2) + dst_offset_z;

  const int sx = get_global_id(0) + src_offset_x;
  const int sy = get_global_id(1) + src_offset_y;
  const int sz = get_global_id(2) + src_offset_z;

  const int4 dpos = (int4){dx,dy,dz,0};
  const int4 spos = (int4){sx,sy,sz,0};

  const DTYPE_IN out = READ_IMAGE_3D(src,sampler,spos).x;
  WRITE_IMAGE_3D(dst,dpos, CONVERT_DTYPE_OUT(out));
}
