#include <iostream>
#include <shared_mutex>
#include <mutex>
#include <thread>
#include <vector>
#include <chrono>


void incBySum(int &val1, int &val2, std::mutex& mtx1, std::mutex& mtx2) {
    std::scoped_lock lock(mtx1, mtx2);
    int sum = val1 + val2;
    val1 += sum;
    val2 += sum;
    std::cout << "incBySumFunc: " << "\n\t sum: " << sum << "\n\t new val1: " << val1 
        << "\n\t new val2: " << val2 << std::endl;
}

void decBySum(int& val1, int& val2, std::mutex& mtx1, std::mutex& mtx2) {
    std::scoped_lock lock(mtx1, mtx2);
    int sum = val1 + val2;
    val1 -= sum;
    val2 -= sum;
    std::cout << "decBySumFunc: " << "\n\t sum: " << sum << "\n\t new val1: " << val1
        << "\n\t new val2: " << val2 << std::endl;
}

void readSharedFunc(int& shared_data, std::shared_mutex& shared_mtx) {
    try {
        std::shared_lock<std::shared_mutex> lock(shared_mtx);
        std::cout << "\nRead: " << shared_data;
    }
    catch (const std::exception& e) {
        std::cerr << "\nRead: " << e.what();
    }
}

void incSharedFunc( int& shared_data, std::shared_mutex& shared_mtx) {
    try {
        std::unique_lock<std::shared_mutex> lock(shared_mtx);
        ++shared_data;
        std::cout << "\nWrite: " << shared_data;
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
    }
    catch (const std::exception& e) {
        std::cerr << "\nWrite: " << e.what();
    }
}

int main() {
    std::vector<std::thread> threads;

    int shared_data = 0;
    std::shared_mutex shared_mtx;
    std::cout << "Read/Write synchronization (shared_mutex)\n";
    for (int i = 0; i < 5; ++i) {
        threads.emplace_back(readSharedFunc, std::ref(shared_data), std::ref(shared_mtx));
        threads.emplace_back(readSharedFunc, std::ref(shared_data), std::ref(shared_mtx));
        threads.emplace_back(incSharedFunc, std::ref(shared_data), std::ref(shared_mtx));
    }

    for (auto& t : threads)
        t.join();

    threads.clear();

    std::cout << std::endl << "\nInc/Dec vals by sum (Each value has its own mutex)\n";
    int val1 = rand() % 100;
    int val2 = rand() % 100;
    std::cout << "Value 1: " << val1 << "\nValue 2: " << val2 << std::endl;
    std::mutex mtx1, mtx2;
    for (int i = 0; i < 5; ++i) {
        threads.emplace_back(incBySum, std::ref(val1), std::ref(val2), std::ref(mtx1), std::ref(mtx2));
        threads.emplace_back(decBySum, std::ref(val1), std::ref(val2), std::ref(mtx1), std::ref(mtx2));
    }

    for (auto& t : threads)
        t.join();

    return 0;
}
