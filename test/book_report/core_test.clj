(ns book-report.core-test
  (:require [clojure.test :refer :all]
            [book-report.core :refer :all]))

(deftest format-return-test
  (testing "Should return a string with no indentation with 1 or less line"
    (is (= (format-return [1 2 3] [])
           "[1 2 3]"))
    (is (= (format-return [1 2 3] ["line"])
           "[1 2 3]")))
  (testing "Should return a string indented with 2 or more lines"
    (is (= (format-return [1 2 3] ["line 1" "line 2"])
           "      [1 2 3]"))))

(deftest append-test
  (testing "Should append an item to a list without formatter function"
    (is (= (append 4 [1 2 3])
           [1 2 3 4])))
  (testing "Should append an item to a list with formatter function"
    (is (= (append (fn [x _] (inc x)) 3 [1 2 3])
           [1 2 3 4]))))

(deftest format-note-test
  (testing "Should append indented lines to a note str"
    (is (= (format-note ["hello" "world"])
           ["hello" "     world"]))))
