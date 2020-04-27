const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__kernel void separable_operation(OUTPUT_IMAGE_PARAMETER, INPUT_IMAGE_PARAMETER, __constant float* kernelValues)
{
  const long x_group = get_group_id(0);
  const long x_local = get_local_id(0);
  const long x = x_group * BLOCK_SIZE + x_local;
  const long y = get_global_id(1);
  const long z = get_global_id(2);

  __local float inputLocal[BLOCK_SIZE + KERNEL_LENGTH - 1];

  for(long i = 0; i < BLOCK_SIZE + KERNEL_LENGTH - 1; i += BLOCK_SIZE)
    if(x_local + i < BLOCK_SIZE + KERNEL_LENGTH - 1)
      inputLocal[x_local + i] = INPUT_READ_PIXEL(x + i, y, z);

  barrier(CLK_LOCAL_MEM_FENCE);

  float result = 0;
  for(long i = 0; i < KERNEL_LENGTH; i++)
    result += kernelValues[i] * inputLocal[x_local + i];

  OUTPUT_WRITE_PIXEL(x, y, z, result);
}
