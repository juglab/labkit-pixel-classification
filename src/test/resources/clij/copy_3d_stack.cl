__kernel void copy_3d_stack(DTYPE_IMAGE_OUT_3D dst, DTYPE_IMAGE_IN_3D src, int offset, int step) {
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);

  const int dz = z * step + offset;

  const int4 dpos = (int4){x,y,dz,0};
  const int4 spos = (int4){x,y,z,0};

  const DTYPE_IN out = READ_IMAGE_3D(src,sampler,spos).x;
  WRITE_IMAGE_3D(dst,dpos, CONVERT_DTYPE_OUT(out));
}
