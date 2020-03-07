#define CALCULATE_INDEX(image, x, y, z) ((z * GET_IMAGE_HEIGHT(image) + y) * GET_IMAGE_WIDTH(image) + x)
#define PIXEL_AT(image, x, y, z) (image[CALCULATE_INDEX(image, x, y, z)])
#define PIXEL(image) PIXEL_AT(image, x, y, z)

__kernel void gauss( IMAGE_output_TYPE output, IMAGE_input_TYPE input, IMAGE_kernelValues_TYPE kernelValues, int skip)
{
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);

  const int kernelLength = GET_IMAGE_WIDTH(kernelValues);
  const int inputOffset = CALCULATE_INDEX(input, x, y, z);

  float result = 0;
  for(int i = 0; i < kernelLength; i++)
    result += kernelValues[i] * input[i * skip + inputOffset];

  output[CALCULATE_INDEX(output, x, y, z)] = result;
}
