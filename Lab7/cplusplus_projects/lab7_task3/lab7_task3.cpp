#include <iostream>
#include <vector>
#include <cmath>
#include <future>
#include <chrono>

bool isPrime(int x) {
    if (x <= 1) return false;
    int top = (int)sqrt(x);
    for (int i = 2; i <= top; i++)
        if (x % i == 0)
            return false;
    return true;
}


std::vector<int> countPrimes(int start, int end) {
    std::vector<int> primes;
    for (int i = start; i <= end; i++)
        if (isPrime(i))
            primes.push_back(i);
    return primes;
}


int main() {
    int MIN = 2;
    int MAX = 100'000'000;

    int num_threads = 0;
    while (num_threads < 1 || num_threads > 30) {
        std::cout << "How many threads do you want to use (from 1 to 30)? ";
        std::cin >> num_threads;
        if (num_threads < 1 || num_threads > 30)
            std::cout << "Please enter a number between 1 and 30!" << std::endl;
    }


    // Sequential program
    auto startSeqTime = std::chrono::high_resolution_clock::now();
    std::vector<int> primesSeq = countPrimes(MIN, MAX);
    auto endSeqTime = std::chrono::high_resolution_clock::now();
    std::chrono::duration<double> durationSeqTime = endSeqTime - startSeqTime;
    std::cout << "Sequential program counted " << primesSeq.size() << " primes in " 
        << durationSeqTime.count() << "s" << std::endl;

    // Multithreaded program
    int step = (MAX - MIN + 1) / num_threads;
    std::vector<std::future<std::vector<int>>> futures;

    auto startParTime = std::chrono::high_resolution_clock::now();
    for (int i = 0; i < num_threads; ++i) {
        int start = MIN + i * step;
        int end = (i == num_threads - 1) ? MAX : start + step - 1;
        futures.push_back(std::async(std::launch::async, countPrimes, start, end));
    }
    std::vector<int> primes;
    for (auto& fut : futures) {
        std::vector<int> future_primes = fut.get();
        primes.insert(primes.end(), future_primes.begin(), future_primes.end());
    }

    auto endParTime = std::chrono::high_resolution_clock::now();
    std::chrono::duration<double> durationParTime = endParTime - startParTime;
    std::cout << "Multithreaded program counted " << primes.size() << " primes in " 
        << durationParTime.count() << "s" << std::endl;
    return 0;
}
