#include<iostream>
#include<vector>
#include<thread>
#include<numeric>

void sumFunc(const std::vector<int>& arr, long long& sum, int startIdx, int endIdx) {
    sum = std::accumulate(arr.begin() + startIdx, arr.begin() + endIdx, 0LL);
}

int main() {
    const int MAX = 100'000'000;

    srand(time(0));
    std::vector<int> arr(MAX);
    for (int i = 0; i < MAX; i++) {
        arr[i] = rand() % 101;
    }

    int numberOfThreads = 0;
    while (numberOfThreads < 1 || numberOfThreads > 30) {
        std::cout << "How many threads do you want to use (from 1 to 30)? ";
        std::cin >> numberOfThreads;
        if (numberOfThreads < 1 || numberOfThreads > 30)
            std::cout << "Please enter a number between 1 and 30!" << std::endl;
    }

    std::vector<std::thread> threads(numberOfThreads);
    std::vector<long long> sums(numberOfThreads);

    auto start = std::chrono::high_resolution_clock::now();
    int chunkSize = MAX / numberOfThreads;
    for (int i = 0; i < numberOfThreads; i++) {
        int startIdx = i * chunkSize;
        int endIdx = (i == numberOfThreads - 1) ? MAX : (i + 1) * chunkSize;
        threads[i] = std::thread(sumFunc, std::ref(arr), std::ref(sums[i]), startIdx, endIdx);
    }

    for (auto& thread : threads)
        thread.join();

    long long sum = std::accumulate(sums.begin(), sums.end(), 0LL);
    auto end = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end - start);

    std::cout << "Program counted sum = " << sum << std::endl;
    std::cout << "Time: " << duration.count() / 1000.0 << " s" << std::endl;

    return 0;
}
