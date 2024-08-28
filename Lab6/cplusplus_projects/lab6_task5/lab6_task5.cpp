#include <iostream>
#include <vector>
#include <thread>
#include <functional>
#include <chrono>
#include <iomanip>
#include <math.h>
#include <numeric> 


template <typename T>
struct integration_block {
    T func;
    double a, b;
    unsigned long n;

    integration_block(T func, double a, double b, unsigned long n)
        : func(func), a(a), b(b), n(n) {}

    void operator()(unsigned long start, unsigned long end, double& result) {
        double h = (b - a) / n;
        result = 0.0;
        for (unsigned long i = start; i < end; i++) {
            double x = a + i * h;
            result += func(x);
        }
        result *= h;
    }
};

template <typename T>
double parallel_integration(T f, double a, double b, unsigned long n) {
    const unsigned long length = n;

    if (!length)
        return 0.0;

    const unsigned long min_per_thread = 25;
    const unsigned long max_threads = (length + min_per_thread - 1) / min_per_thread;

    const unsigned long hardware_threads = std::thread::hardware_concurrency();

    const unsigned long num_threads = std::min(hardware_threads != 0 ? hardware_threads : 2, max_threads);

    const unsigned long block_size = length / num_threads;

    std::vector<double> results(num_threads);
    std::vector<std::thread> threads(num_threads - 1);

    integration_block<T> integral(f, a, b, n);

    unsigned long block_start = 0;
    auto start = std::chrono::high_resolution_clock::now();
    for (unsigned long i = 0; i < (num_threads - 1); ++i) {
        unsigned long block_end = block_start + block_size;
        threads[i] = std::thread(integral, block_start, block_end, std::ref(results[i]));
        block_start = block_end;
    }

    integral(block_start, length, results[num_threads - 1]);

    for (auto& thread : threads) {
        thread.join();
    }

    double result = std::accumulate(results.begin(), results.end(), 0.0);
    auto end = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count();
    std::cout << "Time: " << duration / 1000.0 << "s, " ;

    return result;
}

void integration(std::string funcName, std::function<double(double)> func, double a, double b) {
    std::cout << "Function: " << funcName << std::endl;
    for (int dim = 1'000'000; dim <= 200'000'000; dim *= (dim == 1'000'000 ? 10 : (dim == 10'000'000 ? 10 : 2))) {
        std::cout << "\tDimension: " << dim << ", ";
        for (int threads = 2; threads <= 4; threads *= 2) {
            std::cout << "\n\t\tThreads: " << threads << ", ";
            double result = parallel_integration(func, a, b, dim);
            std::cout << "Result: " << result << std::endl;
        }
    }
    std::cout << std::endl;
}

double func1(double x) { 
    return std::pow(std::sqrt(x * x + 5 * x), 3); 
}
double func2(double x) { 
    return std::exp(std::pow(x, 4)); 
}
double func3(double x) { 
    return std::sin(4 * x - std::sqrt(x)); 
}

int main() {
    std::vector<std::string> funcsNames = { "sqrt(x * x + 5 * x) ^ 3", "exp(x^4)", "sin(4x - sqrt(x))" };
    std::vector<std::function<double(double)>> funcs = { func1, func2, func3 };
    double a = 0.0;
    double b = 1.0;
    for (int i = 0; i < funcs.size(); i++)
        integration(funcsNames[i], funcs[i], a, b);

    return 0;
}

