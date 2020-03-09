__kernel void convolve1d(__global float* output, __global float* input, __constant float* kernelValues)
{
  const long x_group = get_group_id(0);
  const long x_local = get_local_id(0);
  const long x = x_group * BLOCK_SIZE + x_local;
  const long y = get_global_id(1);
  const long z = get_global_id(2);

  const long input_index = INPUT_OFFSET + INPUT_X_SKIP * x + INPUT_Y_SKIP * y + INPUT_Z_SKIP * z;
  const long output_index = OUTPUT_OFFSET + OUTPUT_X_SKIP * x + OUTPUT_Y_SKIP * y + OUTPUT_Z_SKIP * z;

  __local float inputLocal[BLOCK_SIZE + KERNEL_LENGTH - 1];

  for(long i = 0; i < BLOCK_SIZE + KERNEL_LENGTH - 1; i += BLOCK_SIZE)
    if(x_local + i < BLOCK_SIZE + KERNEL_LENGTH - 1)
      inputLocal[x_local + i] = input[i * INPUT_X_SKIP + input_index];

  barrier(CLK_LOCAL_MEM_FENCE);

  float result = 0;
  for(long i = 0; i < KERNEL_LENGTH; i++)
    result += kernelValues[i] * inputLocal[i + x_local];

  barrier(CLK_LOCAL_MEM_FENCE);

  output[output_index] = result;
}
