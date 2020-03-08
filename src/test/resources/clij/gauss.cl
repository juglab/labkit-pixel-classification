#define CALCULATE_INDEX(image, x, y, z) ((z * GET_IMAGE_HEIGHT(image) + y) * GET_IMAGE_WIDTH(image) + x)
#define PIXEL_AT(image, x, y, z) (image[CALCULATE_INDEX(image, x, y, z)])
#define PIXEL(image) PIXEL_AT(image, x, y, z)

__kernel void gauss(
    long image_size_output_width, long image_size_output_height, long image_size_output_depth, __global float* output,
    long image_size_input_width, long image_size_input_height, long image_size_input_depth, __global float* input,
    long image_size_kernelValues_width, long image_size_kernelValues_height, long image_size_kernelValues_depth, __constant float* kernelValues,
    int skip)
{
  const long x_group = get_group_id(0);
  const long x_local = get_local_id(0);
  const long x = x_group * BLOCK_SIZE + x_local;
  const long y = get_global_id(1);
  const long z = get_global_id(2);

  const long kernelLength = GET_IMAGE_WIDTH(kernelValues);
  const long inputOffset = CALCULATE_INDEX(input, x_group, y, z);

  __local float inputLocal[BLOCK_SIZE + KERNEL_LENGTH - 1];

  for(long i = 0; i < BLOCK_SIZE + KERNEL_LENGTH - 1; i += BLOCK_SIZE)
    if(x_local + i < BLOCK_SIZE + KERNEL_LENGTH - 1)
      inputLocal[x_local + i] = input[x_local + i + inputOffset];

  barrier(CLK_LOCAL_MEM_FENCE);

  float result = 0;
  for(long i = 0; i < KERNEL_LENGTH; i++)
    result += kernelValues[i] * inputLocal[i + x_local];

  barrier(CLK_LOCAL_MEM_FENCE);

  output[CALCULATE_INDEX(output, x, y, z)] = result;
}
