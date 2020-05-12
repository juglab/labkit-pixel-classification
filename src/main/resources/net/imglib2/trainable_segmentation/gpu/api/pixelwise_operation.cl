#define PRECISION ldexp(1.0f, -22)

// Returns 1 / sqrt(value)
inline double precise_rsqrt(double value) {
    // The opencl function rsqrt, might not give precise results.
    // This function uses the Newton method to improve the results precision.
    double x2 = value * 0.5;
    double y = rsqrt(value);
    y = y * ( 1.5 - ( x2 * y * y ) );   // Newton
    y = y * ( 1.5 - ( x2 * y * y ) );   // Newton
    y = y * ( 1.5 - ( x2 * y * y ) );   // Newton
    y = y * ( 1.5 - ( x2 * y * y ) );   // Newton
    y = y * ( 1.5 - ( x2 * y * y ) );   // Newton
    return y;
}

// Return the square root of value.
// This method has higher precision than opencl's sqrt() method.
inline double precise_sqrt(double value) {
    return value * precise_rsqrt(value);
}

inline void swap(double x[], int a, int b) {
    double tmp = x[a];
    x[a] = x[b];
    x[b] = tmp;
}

// Calculates the two solutions of the equation: x^2 + c1 * x + c0 == 0
// The results are written to x[], smaller value first.
inline void solve_quadratic_equation(double c0, double c1, double x[]) {
    double p = 0.5 * c1;
    double dis = p * p - c0;
    dis = (dis > 0) ? precise_sqrt(dis) : 0;
    x[0] = (-p - dis);
    x[1] = (-p + dis);
}

// One iteration of Halleys method applied to the depressed cubic equation:
//  x^3 + b1 * x + b0 == 0
inline double halleys_method(double b0, double b1, double x) {
    double dy = 3 * x * x + b1;
    double y = (x * x + b1) * x + b0;	/* ...looks odd, but saves CPU time */
    double dx = y * dy / (dy * dy - 3 * y * x);
    return dx;
}

// Returns one solution to the depressed cubic equation:
//  x^3 + b1 * x + b0 == 0
inline double find_root(double b0, double b1) {
    if(b0 == 0)
        return 0;
    double w = max(fabs(b0), fabs(b1)) + 1.0; /* radius of root circle */
    double h = (b0 > 0.0) ? -w : w;
    double dx;
    do {					/* find 1st root by Halley's method */
        dx = halleys_method(b0, b1, h);
        h -= dx;
    } while (fabs(dx) > fabs(PRECISION * w));
    return h;
}

// Returns all three real solutions of the depressed cubic equation:
//  x^3 + b1 * x + b0 == 0
// The solutions are written to x[]. Smallest solution first.
inline void solve_cubic_scaled_equation(double b0, double b1, double x[]) {
    double h = find_root(b0, b1);
    x[2] = h;
    double c1 = h;			/* deflation; c2 is 1 */
    double c0 = c1 * h + b1;
    solve_quadratic_equation(c0, c1, x);
    if (x[1] > x[2]) {			/* sort results */
        swap(x, 1, 2);
        if (x[0] > x[1]) swap(x, 0, 1);
    }
}

inline int exponent_of(double f) {
    int exponent;
    frexp(f, &exponent);
    return exponent;
}

// Returns all three real solutions of the depressed cubic equation:
//  x^3 + b1 * x + b0 == 0
// The solutions are written to x[]. Smallest solution first.
inline void solve_depressed_cubic_equation(double b0, double b1, double x[]) {
    int e0 = exponent_of(b0) / 3;
    int e1 = exponent_of(b1) / 2;
    int e = - max(e0, e1);
    double scaleFactor = ldexp(1.0, -e);
    b1 = ldexp(b1, 2*e);
    b0 = ldexp(b0, 3*e);
    solve_cubic_scaled_equation(b0, b1, x);
    x[0] *= scaleFactor;
    x[1] *= scaleFactor;
    x[2] *= scaleFactor;
}

// Returns all three real solutions of the cubic equation:
//  x^3 + b2 * x^2 + b1 * x + b0 == 0
// The solutions are written to x[]. Smallest solution first.
inline void solve_cubic_equation(double b0, double b1, double b2, double x[]) {
    double s = 1.0 / 3.0 * b2;
    double q = (2. * s * s - b1) * s + b0;
    double p = b1 - b2 * s;
    solve_depressed_cubic_equation(q, p, x);
    x[0] = x[0] - s;
    x[1] = x[1] - s;
    x[2] = x[2] - s;
}

const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

#define CALCULATE_INDEX(image, x, y, z) (((z) * GET_IMAGE_HEIGHT(image) + (y)) * GET_IMAGE_WIDTH(image) + (x))
#define PIXEL_OFFSET(image, offset) (image[CALCULATE_INDEX(image, coordinate_x, coordinate_y, coordinate_z) + (offset)])
#define PIXEL(image) (image[CALCULATE_INDEX(image, coordinate_x, coordinate_y, coordinate_z)])

__kernel void operation(PARAMETER) {
  const int coordinate_x = get_global_id(0);
  const int coordinate_y = get_global_id(1);
  const int coordinate_z = get_global_id(2);
  OPERATION;
}
