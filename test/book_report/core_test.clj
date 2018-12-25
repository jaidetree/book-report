(ns book-report.core-test
  (:require [clojure.test :refer :all]
            [book-report.core :refer :all]
            [clojure.string :refer [split]]))

(deftest format-return-test
  (testing "Should return a string with no indentation with 1 or less line"
    (is (= "[1 2 3]"
           (format-return [1 2 3] [])))
    (is (= "[1 2 3]"
           (format-return [1 2 3] ["line"]))))
  (testing "Should return a string indented with 2 or more lines"
    (is (= "      [1 2 3]"
           (format-return [1 2 3] ["line 1" "line 2"])))))

(deftest append-test
  (testing "Should append an item to a list without formatter function"
    (is (= [1 2 3 4]
           (append 4 [1 2 3]))))
  (testing "Should append an item to a list with formatter function"
    (is (= [1 2 3 4]
           (append (fn [v _] (inc v)) 3 [1 2 3])))))

(deftest format-note-test
  (testing "Should append indented lines to a note str"
    (is (= ["hello" "     world"]
           (format-note ["hello" "world"])))))


(deftest format-notes-test
  (testing "Should return a list of clojure expressions to print notes"
    (is (= `(println "  Notes:\n"
                     "   - hello\n   - world\n     again.")
           (format-notes ["hello" "world\nagain."])))))

(deftest format-code-lines-test
  (testing "Should show forms as 2-space indented strings."
    (is (= "  hello\n  world"
           (format-code-lines ["hello" "world"])))))

(deftest space-code-blocks-test
  (testing "Should join single-line groups with a single new line"
    (is (= "hello world\n"
           (space-code-blocks "" "hello world"))))
  (testing "Should join mlti-line groups with 2 new lines"
   (is (= "hello\nworld\n\n"
          (space-code-blocks "" "hello\nworld")))))

(deftest format-code-test
  (testing "Should take a list of forms and return a list of forms"
    (is (= `(println "  (+ 1 2)")
           (format-code '((+ 1 2)))))))

(deftest with-out-str-and-value-test
  (testing "Should capture text output and return a value"
    (is (= ["hello world\n" 3]
           (with-out-str-and-value
             (do (println "hello world")
                 (+ 1 2)))))))

(deftest run-code-test
  (testing "Should return a list of forms in a do block"
    (is (= '(do (println "hello world")
                (+ 1 2))
            (run-code '((println "hello world") (+ 1 2)))))))

(deftest eval-str-test
  (testing "Should evaluate a code form and return the output and results"
    (is (= ["hello world\n" 3]
           (eval-str '(do (println "hello world") (+ 1 2)))))))

(deftest format-eval-test
  (testing "Should evaluate forms and format the output as indented lines"
    (is (= `(println "   ➜" "hello world\n      3")
           (format-eval '((println "hello world") (+ 1 2))))))
  (testing "Should format lines without any output"
   (is (= `(println "   ➜" "3")
          (format-eval '((+ 1 2)))))))

(deftest format-title-test
  (testing "Should return a list of forms to print an underlined title"
    (is (= `(println "  # Title\n  –––––––––")
           (format-title "Title")))))

(deftest eval-form-test
  (testing "Should return true if form should be evaluated"
    (is (= '(true true true true true true)
           (map eval-form? '((+ 1 2) (println "hello") 5 "hi" :test {:pass true})))))
  (testing "Should return false if case form should not be evaluted"
    (is (= [false false false false]
           (map eval-form? '((:book-report.core/value) (notes) (run) (title)))))))

(deftest lesson-test
  (testing "Should output a formatted lesson to stdout"
    (is
     (let [output (split (with-out-str (lesson 1 "Lesson title"
                                               (notes "Note")
                                               (+ 1 2)
                                               (title "Title")
                                               (run (def x 3))
                                               (+ x 1)))
                         #"\n")]
       (= ["Chapter 1 :: Lesson title"
              ""
              "  Notes:"
              "    - Note"
              ""
              "  (+ 1 2)"
              "   ➜ 3"
              ""
              "  # Title"
              "  –––––––––"
              ""
              "  (+ x 1)"
              "   ➜ 4"
              ""
             output])))))
