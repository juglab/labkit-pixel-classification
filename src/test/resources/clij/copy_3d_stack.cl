__kernel void copy_3d_stack(IMAGE_dst_TYPE dst, IMAGE_src_TYPE src, int offset, int step) {
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);

  const int dz = z * step + offset;

  const int4 dpos = (int4){x,y,dz,0};
  const int4 spos = (int4){x,y,z,0};

  const IMAGE_dst_PIXEL_TYPE out = (IMAGE_dst_PIXEL_TYPE) READ_src_IMAGE(src,sampler,spos).x;
  WRITE_dst_IMAGE(dst, dpos, out);
}
