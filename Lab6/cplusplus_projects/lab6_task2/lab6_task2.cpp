#include <iostream>
#include <queue>
#include <thread>
#include <mutex>
#include <chrono>
#include <cmath>

struct Point {
    double x, y;
    Point(double x, double y) : x(x), y(y) {}
};

class Producer {
private:
    std::queue<Point>& queue;
    int numPoints;
    std::mutex& mtx;
    std::condition_variable& cv;

public:
    Producer(std::queue<Point>& queue, int numPoints, std::mutex& mtx, std::condition_variable& cv) :
        queue(queue), numPoints(numPoints), mtx(mtx), cv(cv){}

    void run() {
        for (int i = 0; i < numPoints; i++) 
        {
            double x = rand() / (RAND_MAX + 1.0);
            double y = rand() / (RAND_MAX + 1.0);
            {
                std::unique_lock<std::mutex> lock(mtx);
                queue.push(Point(x, y));
            }
            cv.notify_one();
        }

    }
};

class Consumer {
private:
    std::queue<Point>& queue;
    int insideCircle;
    std::mutex& mtx;
    bool &stopFlag;
    std::condition_variable& cv;

public:
    Consumer(std::queue<Point>& queue, std::mutex& mtx, bool &stopFlag, std::condition_variable& cv) :
        queue(queue), insideCircle(0), mtx(mtx), stopFlag(stopFlag), cv(cv) {}

    int getInsideCircle() {
        return insideCircle;
    }

    void run() {
        while (true) {
            std::unique_lock<std::mutex> lock(mtx);
            cv.wait(lock, [this] { return !queue.empty() || stopFlag; });

            if (stopFlag && queue.empty()) {
                break;
            }

            Point point = queue.front();
            queue.pop();
            lock.unlock();

            if (point.x * point.x + point.y * point.y <= 1)
                insideCircle++;
        }
    }
};

int main() {
    int numPoints = 1'000'000;
    std::queue<Point> queue;
    std::mutex mtx;
    bool stopFlag = false;
    std::condition_variable cv;

    auto startTime = std::chrono::high_resolution_clock::now();

    Producer* producers[2];
    Consumer* consumers[2];
    std::thread producerThreads[2];
    std::thread consumerThreads[2];

    for (int i = 0; i < 2; i++) {
        producers[i] = new Producer(queue, numPoints/2, mtx, cv);
        consumers[i] = new Consumer(queue, mtx, stopFlag, cv);
        producerThreads[i] = std::thread(&Producer::run, producers[i]);
        consumerThreads[i] = std::thread(&Consumer::run, consumers[i]);
    }

    for (int i = 0; i < 2; i++) {
        producerThreads[i].join();
    }

    stopFlag = true;
    cv.notify_all();
    for (int i = 0; i < 2; i++) {
        consumerThreads[i].join();
    }   

    int totalInsideCircle = 0;
    for (int i = 0; i < 2; i++) {
        totalInsideCircle += consumers[i]->getInsideCircle();
    }

    auto endTime = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(endTime - startTime);

    std::cout << "Final estimation of Pi: " << 4 * (double)totalInsideCircle / (numPoints) << std::endl;
    std::cout << "Total execution time: " << duration.count()/1000.0 << "s" << std::endl;

    return 0;
}


