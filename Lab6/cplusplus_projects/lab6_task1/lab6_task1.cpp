//#include <iostream>
//#include <vector>
//#include <thread>
//#include <cmath>
//#include <chrono>
//
//const int MAX = 10'000'000;
//const int cycle = 100;
//
//bool isPrime(int x) {
//    if (x <= 1) return false;
//    int top = (int)sqrt(x);
//    for (int i = 2; i <= top; i++)
//        if (x % i == 0)
//            return false;
//    return true;
//}
//
//int countPrimes(int start, int end) {
//    int count = 0;
//    for (int i = start; i < end; i++)
//        if (isPrime(i))
//            count++;
//    return count;
//}
//
//class CountPrimesThread {
//public:
//    CountPrimesThread(int id, int start, int threadsNum, int count) : id(id), count(count), start(start), threadsNum(threadsNum) {}
//
//    int getCount() {
//        return count;
//    }
//
//    void operator()() {
//        auto startTime = std::chrono::high_resolution_clock::now();
//        int i;
//        for (i = start; i < MAX - cycle; i += cycle * threadsNum)
//            count += countPrimes(i, i + cycle);
//        if (i + cycle - MAX < 100)
//            count += countPrimes(i, MAX + 1);
//        auto elapsedTime = std::chrono::high_resolution_clock::now() - startTime;
//        std::cout << "Thread " << id << " counted " << count << " primes in " << std::chrono::duration<double>(elapsedTime).count() << " seconds." << std::endl;
//    }
//
//private:
//    int id;
//    int count;
//    int start;
//    int threadsNum;
//};
//
//int main() {
//    int numberOfThreads = 0;
//    while (numberOfThreads < 1 || numberOfThreads > 30) {
//        std::cout << "How many threads do you want to use (from 1 to 30)? ";
//        std::cin >> numberOfThreads;
//        if (numberOfThreads < 1 || numberOfThreads > 30)
//            std::cout << "Please enter a number between 1 and 30!" << std::endl;
//    }
//
//    // Sequential program
//    std::cout << "\nRunning sequential program..." << std::endl;
//    auto startTimeSeq = std::chrono::high_resolution_clock::now();
//    int countSeq = countPrimes(2, MAX + 1);
//    auto elapsedTimeSeq = std::chrono::high_resolution_clock::now() - startTimeSeq;
//    std::cout << "Sequential program counted " << countSeq << " primes in " << std::chrono::duration<double>(elapsedTimeSeq).count() << " seconds." << std::endl;
//
//    // Multithreaded program
//    std::cout << "\nRunning multithreaded program with " << numberOfThreads << " threads..." << std::endl;
//    std::vector<std::thread> workers;
//    std::vector<int> counts(numberOfThreads, 0);
//
//    auto startTimeMulti = std::chrono::high_resolution_clock::now();
//    for (int i = 0; i < numberOfThreads; i++) {
//        int start = i * cycle + 2;
//        workers.push_back(std::thread(CountPrimesThread(i, start, numberOfThreads, std::ref(counts[i]))));
//    }
//
//    for (auto& worker : workers)
//        worker.join();
//
//    int countMulti = 0;
//    for (int count : counts)
//        countMulti += count;
//
//    auto elapsedTimeMulti = std::chrono::high_resolution_clock::now() - startTimeMulti;
//    std::cout << "Multithreaded program counted " << countMulti << " primes in " << std::chrono::duration<double>(elapsedTimeMulti).count() << " seconds." << std::endl;
//
//    return 0;
//}




#include <iostream>
#include <vector>
#include <thread>
#include <cmath>
#include <chrono>

const int MAX = 10'000'000;
const int cycle = 100;

bool isPrime(int x) {
    if (x <= 1) return false;
    int top = (int)sqrt(x);
    for (int i = 2; i <= top; i++)
        if (x % i == 0)
            return false;
    return true;
}

int countPrimes(int start, int end) {
    int count = 0;
    for (int i = start; i < end; i++)
        if (isPrime(i))
            count++;
    return count;
}

class CountPrimesThread {
public:
    CountPrimesThread(int id, int start, int threadsNum, int* counts) : id(id), start(start), threadsNum(threadsNum), counts(counts) {}

    void operator()() {
        auto startTime = std::chrono::steady_clock::now();
        int i;
        for (i = start; i < MAX - cycle; i += cycle * threadsNum)
            count += countPrimes(i, i + cycle);
        if (i + cycle - MAX < 100)
            count += countPrimes(i, MAX + 1);
        auto elapsedTime = std::chrono::steady_clock::now() - startTime;
        std::cout << "Thread " << id << " counted " << count << " primes in " << std::chrono::duration<double>(elapsedTime).count() << " seconds." << std::endl;
        counts[id] = count;
    }

private:
    int id;
    int count;
    int start;
    int threadsNum;
    int* counts;
};

int main() {
    int numberOfThreads = 0;
    while (numberOfThreads < 1 || numberOfThreads > 30) {
        std::cout << "How many threads do you want to use (from 1 to 30)? ";
        std::cin >> numberOfThreads;
        if (numberOfThreads < 1 || numberOfThreads > 30)
            std::cout << "Please enter a number between 1 and 30!" << std::endl;
    }

    // Sequential program
    std::cout << "\nRunning sequential program..." << std::endl;
    auto startTimeSeq = std::chrono::steady_clock::now();
    int countSeq = countPrimes(2, MAX + 1);
    auto elapsedTimeSeq = std::chrono::steady_clock::now() - startTimeSeq;
    std::cout << "Sequential program counted " << countSeq << " primes in " << std::chrono::duration<double>(elapsedTimeSeq).count() << " seconds." << std::endl;

    // Multithreaded program
    std::cout << "\nRunning multithreaded program with " << numberOfThreads << " threads..." << std::endl;
    std::vector<std::thread> workers(numberOfThreads);
    std::vector<int> counts(numberOfThreads, 0);

    auto startTimeMulti = std::chrono::steady_clock::now();
    for (int i = 0; i < numberOfThreads; i++) {
        int start = i * cycle + 2;
        workers[i] = std::thread(CountPrimesThread(i, start, numberOfThreads, counts.data()));
    }

    for (auto& worker : workers)
        worker.join();

    int countMulti = 0;
    for (int count : counts)
        countMulti += count;

    auto elapsedTimeMulti = std::chrono::steady_clock::now() - startTimeMulti;
    std::cout << "Multithreaded program counted " << countMulti << " primes in " << std::chrono::duration<double>(elapsedTimeMulti).count() << " seconds." << std::endl;

    return 0;
}










//#include <iostream>
//#include <numeric>
//#include <thread>
//#include <vector>
//bool is_prime(int x) {
//	int top = sqrt(x);
//	for (int i = 2; i <= top; i++)
//		if (x % i == 0) return false;
//	return true;
//}
//struct primes_block {
//	void operator()(int start, int end, int& result) {
//		result = 0;
//		for (int i = start; i <= end; i++)
//			if (is_prime(i)) result++;
//	}
//};
//int parallel_count_primes(int first, int last, int init,
//	int min_per_thread = 2) {
//	unsigned long const length = last - first + 1;
//	if (!length) return init;
//	unsigned long const max_threads =
//		(length + min_per_thread - 1) / min_per_thread;
//	unsigned long const hardware_threads = std::thread::hardware_concurrency();
//	unsigned long const num_threads =
//		std::min(hardware_threads != 0 ? hardware_threads : 2, max_threads);
//	unsigned long const block_size = length / num_threads;
//	std::vector<int> results(num_threads);
//	std::vector<std::thread> threads(num_threads - 1);
//	int block_start = first;
//	for (unsigned long i = 0; i < (num_threads - 1); ++i) {
//		int block_end = block_start + block_size - 1;
//		threads[i] = std::thread(primes_block(), block_start, block_end,
//			std::ref(results[i]));
//		block_start = block_end + 1;
//	}
//	primes_block()(block_start, last, results[num_threads - 1]);
//	for (auto& entry : threads) entry.join();
//	return std::accumulate(results.begin(), results.end(), init);
//}
//int main() {
//	const int MIN = 2;
//	const int MAX = 10'000'000;
//	auto begin = std::chrono::steady_clock::now(); // устойчивые часы
//	int total_primes = parallel_count_primes(MIN, MAX, 0);
//	auto end = std::chrono::steady_clock::now();
//	auto elapsed_ms =
//		std::chrono::duration_cast<std::chrono::milliseconds>(end - begin);
//	std::cout << "The time: " << elapsed_ms.count() << " ms\n";
//	std::cout << "Result: " << total_primes << std::endl;
//	std::cout << "Main thread finishes\n";
//}