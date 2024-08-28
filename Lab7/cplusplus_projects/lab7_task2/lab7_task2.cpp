#include <iostream>
#include <thread>
#include <vector>
#include <mutex>

void sync_print(int id, std::mutex& mtx) {
    for (int i = 0; i < 5; ++i) {
        std::lock_guard<std::mutex> lock(mtx);
        std::cout << "Thread " << id << ": message " << i << std::endl;
    }
}

void unsync_print(int id) {
    for (int i = 0; i < 5; ++i)
        std::cout << "Thread " << id << ": message " << i << std::endl;
}

int main() {
    std::vector<std::thread> threads;
    std::mutex mtx;

    std::cout << "Non-Synchronized execution\n";
    for (int i = 0; i < 5; ++i)
        threads.emplace_back(unsync_print, i);
    for (auto& t : threads)
        t.join();

    threads.clear();
    std::cout << "\nSynchronized execution\n";
    for (int i = 0; i < 5; ++i)
        threads.emplace_back(sync_print, i, std::ref(mtx));
    for (auto& t : threads)
        t.join();
    return 0;
}
