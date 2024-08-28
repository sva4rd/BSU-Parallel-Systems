#include <iostream>
#include <vector>
#include <algorithm>
#include <future>
#include <iterator>
#include <chrono>


int find_max_parallel(const std::vector<int>& arr, size_t start, size_t end) {
    if (end - start <= 1) 
        return arr[start];

    size_t mid = start + (end - start) / 2;
    auto leftFuture = std::async(std::launch::async, find_max_parallel, std::cref(arr), start, mid);
    int rightResult = find_max_parallel(arr, mid, end);
    int leftResult = leftFuture.get();
    return std::max(leftResult, rightResult);
}

int find_max_sequential(const std::vector<int>& arr, size_t start, size_t end) {
    if (end - start <= 1)
        return arr[start];

    size_t mid = start + (end - start) / 2;
    int leftResult = find_max_sequential(arr, start, mid);
    int rightResult = find_max_sequential(arr, mid, end);
    return std::max(leftResult, rightResult);
}

int main() {

    const int MAX = 1'000;
    srand(time(0));
    std::vector<int> arr(MAX);
    for (int i = 0; i < MAX; i++)
        arr[i] = rand();

    // Sequential program
    auto startTimeSeq = std::chrono::high_resolution_clock::now();
    int maxSeq = find_max_sequential(arr, 0, arr.size());
    std::chrono::duration<double> durationSeq = std::chrono::high_resolution_clock::now() - startTimeSeq;
    std::cout << "Sequential program counted max = " << maxSeq << " in "
        << durationSeq.count() << "s" << std::endl;

    // Multithreaded program
    auto startTimePar = std::chrono::high_resolution_clock::now();
    int maxPar = find_max_parallel(arr, 0, arr.size());
    std::chrono::duration<double> durationPar = std::chrono::high_resolution_clock::now() - startTimePar;
    std::cout << "Multithreaded program counted max = " << maxPar << " in "
        << durationPar.count() << "s" << std::endl;
    
    return 0;
}
