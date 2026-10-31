// CS-300 - Project Two
// February 20, 2026
// Author: Michael Gresh

#include <algorithm>
#include <cctype>
#include <fstream>
#include <iostream>
#include <limits>
#include <set>
#include <sstream>
#include <string>
#include <vector>

struct Course {
    std::string courseNumber;
    std::string courseTitle;
    std::vector<std::string> prerequisites;
};

struct Node {
    Course course;
    Node* left = nullptr;
    Node* right = nullptr;

    explicit Node(const Course& c) : course(c) {}
};

class BinarySearchTree {
public:
    BinarySearchTree() = default;

    BinarySearchTree(const BinarySearchTree&) = delete;
    BinarySearchTree& operator=(const BinarySearchTree&) = delete;

    ~BinarySearchTree() {
        clear();
    }

    void clear() {
        clearRecursive(root);
        root = nullptr;
    }

    void insert(const Course& course) {
        root = insertRecursive(root, course);
    }

    const Course* search(const std::string& courseNumber) const {
        const Node* current = root;
        while (current != nullptr) {
            if (courseNumber == current->course.courseNumber) {
                return &current->course;
            } else if (courseNumber < current->course.courseNumber) {
                current = current->left;
            } else {
                current = current->right;
            }
        }
        return nullptr;
    }

    void printInOrder() const {
        inOrderRecursive(root);
    }

private:
    Node* root = nullptr;

    static void clearRecursive(Node* node) {
        if (node == nullptr) return;
        clearRecursive(node->left);
        clearRecursive(node->right);
        delete node;
    }

    static Node* insertRecursive(Node* node, const Course& course) {
        if (node == nullptr) return new Node(course);

        if (course.courseNumber < node->course.courseNumber) {
            node->left = insertRecursive(node->left, course);
        } else if (course.courseNumber > node->course.courseNumber) {
            node->right = insertRecursive(node->right, course);
        } else {
            // Duplicate course numbers are rejected during load; no action needed here.
        }
        return node;
    }

    static void inOrderRecursive(const Node* node) {
        if (node == nullptr) return;
        inOrderRecursive(node->left);
        std::cout << node->course.courseNumber << ", " << node->course.courseTitle << std::endl;
        inOrderRecursive(node->right);
    }
};

// --------------------------
//      Utility Helpers
// --------------------------

static inline std::string trim(const std::string& s) {
    std::size_t start = 0;
    while (start < s.size() && std::isspace(static_cast<unsigned char>(s[start]))) start++;

    std::size_t end = s.size();
    while (end > start && std::isspace(static_cast<unsigned char>(s[end - 1]))) end--;

    return s.substr(start, end - start);
}

static inline std::string normalizeCourseNumber(std::string s) {
    s = trim(s);

    std::string out;
    out.reserve(s.size());

    for (char ch : s) {
        if (!std::isspace(static_cast<unsigned char>(ch))) {
            out.push_back(static_cast<char>(std::toupper(static_cast<unsigned char>(ch))));
        }
    }

    return out;
}

// Simple CSV split on commas
static std::vector<std::string> splitCSVLine(const std::string& line) {
    std::vector<std::string> tokens;
    std::string token;
    std::stringstream ss(line);
    while (std::getline(ss, token, ',')) {
        tokens.push_back(trim(token));
    }
    return tokens;
}

// ---------------------------------
//      Core Program Functions
// ---------------------------------

static bool loadCoursesFromFile(const std::string& filename, BinarySearchTree& bst) {
    std::ifstream file(filename);
    if (!file.is_open()) {
        std::cout << "Error: Unable to open file \"" << filename << "\"." << std::endl;
        std::cout << "Tip: If the file is in a different folder, enter the full path." << std::endl;
        return false;
    }

    // Pass 1: Read and build courses, track course numbers, and validate minimum format.
    std::vector<Course> loadedCourses;
    std::set<std::string> courseNumbers;

    std::string line;
    int lineNumber = 0;

    while (std::getline(file, line)) {
        lineNumber++;
        line = trim(line);
        if (line.empty()) continue;

        std::vector<std::string> tokens = splitCSVLine(line);
        if (tokens.size() < 2) {
            std::cout << "File format error on line " << lineNumber
                      << ": each line must include courseNumber and courseTitle." << std::endl;
            return false;
        }

        std::string number = normalizeCourseNumber(tokens[0]);
        std::string title = trim(tokens[1]);

        if (number.empty() || title.empty()) {
            std::cout << "File format error on line " << lineNumber
                      << ": courseNumber and courseTitle cannot be blank." << std::endl;
            return false;
        }

        if (courseNumbers.count(number)) {
            std::cout << "File format error on line " << lineNumber
                      << ": duplicate courseNumber \"" << number << "\"." << std::endl;
            return false;
        }

        Course course;
        course.courseNumber = number;
        course.courseTitle = title;

        for (std::size_t i = 2; i < tokens.size(); i++) {
            std::string prereq = normalizeCourseNumber(tokens[i]);
            if (!prereq.empty()) {
                course.prerequisites.push_back(prereq);
            }
        }

        courseNumbers.insert(number);
        loadedCourses.push_back(course);
    }

    // Pass 2: Validate prerequisites exist as course numbers in the file.
    for (const Course& c : loadedCourses) {
        for (const std::string& prereq : c.prerequisites) {
            if (!courseNumbers.count(prereq)) {
                std::cout << "File format error: prerequisite \"" << prereq
                          << "\" not found in file." << std::endl;
                return false;
            }
        }
    }

    // Clear and replace existing data only after the new file is completely validated.
    bst.clear();

    // Insert into BST only after validation succeeds.
    for (const Course& c : loadedCourses) {
        bst.insert(c);
    }

    std::cout << "Courses loaded successfully." << std::endl;
    return true;
}

static void printCourseList(const BinarySearchTree& bst) {
    std::cout << std::endl;
    std::cout << "Here is a sample schedule:" << std::endl;
    bst.printInOrder();
    std::cout << std::endl;
}

static void printSingleCourse(const BinarySearchTree& bst) {
    std::cout << "What course do you want to know about? ";
    std::string input;
    std::getline(std::cin, input);

    std::string query = normalizeCourseNumber(input);
    const Course* course = bst.search(query);

    if (course == nullptr) {
        std::cout << "Course not found." << std::endl;
        return;
    }

    std::cout << course->courseNumber << ", " << course->courseTitle << std::endl;

    if (course->prerequisites.empty()) {
        std::cout << "Prerequisites: None" << std::endl;
        return;
    }

    // Print prerequisites in sorted order and resolve titles.
    std::vector<std::string> prereqs = course->prerequisites;
    std::sort(prereqs.begin(), prereqs.end());

    std::cout << "Prerequisites: ";
    for (std::size_t i = 0; i < prereqs.size(); ++i) {
        std::cout << prereqs[i];
        if (i < prereqs.size() - 1) {
            std::cout << ", ";
        }
    }
    std::cout << std::endl;
}

static int readMenuChoice() {
    // Numeric input handling: accepts an integer and reprompts on non-numeric input.
    int choice;
    while (true) {
        std::cout << "What would you like to do? ";
        if (std::cin >> choice) {
            std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n');
            return choice;
        }

        // Bad input: Reset stream and retry.
        std::cin.clear();
        std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n');
        std::cout << "Invalid input. Please enter a number." << std::endl;
    }
}

static std::string normalizePathInput(std::string s) {
    s = trim(s);

    if (s.size() >= 2) {
        if ((s.front() == '"' && s.back() == '"') || (s.front() == '\'' && s.back() == '\'')) {
            s = s.substr(1, s.size() - 2);
        }
    }
    return s;
}

static void printMenu() {
    std::cout << "1. Load Data Structure." << std::endl;
    std::cout << "2. Print Course List." << std::endl;
    std::cout << "3. Print Course." << std::endl;
    std::cout << "9. Exit" << std::endl;
}

int main() {
    BinarySearchTree bst;
    bool dataLoaded = false;

    std::cout << "Welcome to the course planner." << std::endl;

    int choice = 0;
    while (choice != 9) {
        printMenu();
        choice = readMenuChoice();

        switch (choice) {
            case 1: {
                while (true) {
                    std::cout << "Enter the file name: ";
                    std::string filename;
                    std::getline(std::cin, filename);
                    filename = normalizePathInput(filename);

                    if (filename.empty()) {
                        std::cout << "Error: File name cannot be blank." << std::endl;
                        continue;
                    }

                    dataLoaded = loadCoursesFromFile(filename, bst);
                    if (dataLoaded) break;

                    std::cout << "Try again? (y/n): ";
                    std::string ans;
                    std::getline(std::cin, ans);

                    if (ans.empty() || (ans[0] != 'y' && ans[0] != 'Y')) {
                        break;
                    }
                }
                break;
            }
            case 2: {
                if (!dataLoaded) {
                    std::cout << "Error: Please load data first (Option 1)." << std::endl;
                } else {
                    printCourseList(bst);
                }
                break;
            }
            case 3: {
                if (!dataLoaded) {
                    std::cout << "Error: Please load data first (Option 1)." << std::endl;
                } else {
                    printSingleCourse(bst);
                }
                break;
            }
            case 9:
                std::cout << "Thank you for using the course planner!" << std::endl;
                break;

            default:
                std::cout << choice << " is not a valid option." << std::endl;
                break;
        }
    }

    return 0;
}