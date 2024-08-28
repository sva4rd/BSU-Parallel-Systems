#include <iostream>
#include <vector>
#include <algorithm>
#include <numeric>
#include <execution>
#include <chrono>

int main() {
    const size_t size = 200'000'000;
    std::vector<int> arr(size);
    srand(time(0));
    for (auto x : arr)
        x = rand();

    auto startSeqTime = std::chrono::high_resolution_clock::now();
    std::for_each(arr.begin(), arr.end(), [](int& n) { n /= 3; });
    std::transform(arr.begin(), arr.end(), arr.begin(), [](int n) { return std::pow(std::sqrt(n), 3); });
    int sum = std::reduce(arr.begin(), arr.end());
    auto endSeqTime = std::chrono::high_resolution_clock::now();
    std::chrono::duration<double> duration = endSeqTime - startSeqTime;
    std::cout << "Sequential time: " << duration.count() << "s\n";


    for (auto x : arr)
        x = rand();
    auto startParallel = std::chrono::high_resolution_clock::now();
    std::for_each(std::execution::par, arr.begin(), arr.end(), [](int& n) { n /= 3; });
    std::transform(std::execution::par, arr.begin(), arr.end(), arr.begin(), [](int n) { return std::pow(std::sqrt(n), 3); });
    sum = std::reduce(std::execution::par, arr.begin(), arr.end());
    auto endParallel = std::chrono::high_resolution_clock::now();
    duration = endParallel - startParallel;
    std::cout << "Parallel time: " << duration.count() << "s\n";

    return 0;
}
