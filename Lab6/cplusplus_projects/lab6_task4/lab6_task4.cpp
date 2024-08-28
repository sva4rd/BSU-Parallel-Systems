#include <iostream>
#include <vector>
#include <thread>
#include <numeric>
#include <chrono>

template <typename Iterator, typename T>
struct accumulate_block {
    void operator()(Iterator first, Iterator last, T& result) {
        result = std::accumulate(first, last, result);
    }
};

template <typename Iterator, typename T>
T parallel_accumulate(Iterator first, Iterator last, T init) {
    const unsigned long length = std::distance(first, last);

    if (!length)
        return init;

    const unsigned long min_per_thread = 25;
    const unsigned long max_threads = (length + min_per_thread - 1) / min_per_thread;

    const unsigned long hardware_threads = std::thread::hardware_concurrency();

    const unsigned long num_threads = std::min(hardware_threads != 0 ? hardware_threads : 2, max_threads);

    const unsigned long block_size = length / num_threads;

    std::vector<T> results(num_threads);
    std::vector<std::thread> threads(num_threads - 1);

    Iterator block_start = first;
    auto start = std::chrono::high_resolution_clock::now();
    for (unsigned long i = 0; i < (num_threads - 1); ++i) {
        Iterator block_end = block_start;
        std::advance(block_end, block_size);
        threads[i] = std::thread(accumulate_block<Iterator, T>(), block_start, block_end, std::ref(results[i]));
        block_start = block_end;
    }

    accumulate_block<Iterator, T>()(block_start, last, results[num_threads - 1]);

    for (auto& thread : threads) {
        thread.join();
    }

    T result = std::accumulate(results.begin(), results.end(), init);
    auto end = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count();
    std::cout << "Time: " << duration/1000.0 << " s" << std::endl;

    return result;
}

int main() {
    const int MAX = 100'000'000;
    srand(time(0));
    std::vector<int> arr(MAX);
    for (int i = 0; i < MAX; i++)
        arr[i] = rand() % 101;

    long long sum = parallel_accumulate(arr.begin(), arr.end(), 0LL);
    std::cout << "Program counted sum = " << sum << std::endl;

    return 0;
}
