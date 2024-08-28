#include <iostream>
#include <vector>
#include <algorithm>
#include <future>
#include <numeric>
#include <iterator>
#include <chrono>

int find_max_block(const std::vector<int>& vec, size_t start, size_t end) {
    return *std::max_element(vec.begin() + start, vec.begin() + end);
}

int parallel_find_max(const std::vector<int>& vec, size_t num_threads) {
    size_t length = vec.size();
    size_t block_size = length / num_threads;
    std::vector<std::future<int>> futures;

    for (size_t i = 0; i < num_threads; ++i) {
        size_t start = i * block_size;
        size_t end = (i == num_threads - 1) ? length : (i + 1) * block_size;
        futures.push_back(std::async(std::launch::async, find_max_block, std::cref(vec), start, end));
    }

    int max_value = std::numeric_limits<int>::min();
    for (auto& future : futures)
        max_value = std::max(max_value, future.get());

    return max_value;
}

int main() {
    const int MAX = 100'000'000;
    srand(time(0));
    std::vector<int> arr(MAX);
    for (int i = 0; i < MAX; i++)
        arr[i] = rand();

    size_t num_threads = 0;
    while (num_threads < 1 || num_threads > 30) {
        std::cout << "How many threads do you want to use (from 1 to 30)? ";
        std::cin >> num_threads;
        if (num_threads < 1 || num_threads > 30)
            std::cout << "Please enter a number between 1 and 30!" << std::endl;
    }

    // Sequential program
    auto startTimeSeq = std::chrono::high_resolution_clock::now();
    int maxSeq = *std::max_element(arr.begin(), arr.end());
    std::chrono::duration<double> durationSeq = std::chrono::high_resolution_clock::now() - startTimeSeq;
    std::cout << "Sequential program counted max = " << maxSeq << " in "
        << durationSeq.count() << "s" << std::endl;

    // Multithreaded program
    auto startTimePar = std::chrono::high_resolution_clock::now();
    int maxPar = parallel_find_max(arr, num_threads);
    std::chrono::duration<double> durationPar = std::chrono::high_resolution_clock::now() - startTimePar;
    std::cout << "Multithreaded program counted max = " << maxPar << " in "
        << durationPar.count() << "s" << std::endl;

    return 0;
}
