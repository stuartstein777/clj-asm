(ns asm.parser-test
  (:require [clojure.test :refer :all]
            [asm.parser :refer :all]))

(deftest to-keywords-tests
  (testing "[mov a 5] to [:mov :a 5]"
    (is (= [:mov :a 5] (to-keywords "mov a 5"))))
  (testing "[mov a b] to [:mov :a :b]"
    (is (= [:mov :a :b] (to-keywords "mov a b"))))
  (testing "[inc a] to [:inc :a]"
    (is (= [:inc :a] (to-keywords "inc a"))))
  (testing "[dec a] to [:dec :a]"
    (is (= [:dec :a] (to-keywords "dec a"))))
  (testing "[sub x y] to [:sub :x :y]"
    (is (= [:sub :x :y] (to-keywords "sub x y")))))

(deftest is-register?-tests
  (testing "a should return true"
    (is (true? (is-register? "a"))))
  (testing "5 should return false"
    (is (false? (is-register? "5"))))
  (testing "-1 should return false"
    (is (false? (is-register? "-1")))))

(deftest parsing-tests
  (testing "complex parsing 1"
    (is (= [[:mov :a 5]
            [:inc :a]
            [:call :function]
            [:end]
            [(keyword "function:")]
            [:div :a 2]
            [:ret]]
           (parse "; my first program
                   mov a 5
                   inc a
                   call function
                   end
                   function:
                   div a 2
                   ret"))))
  (testing "complex parsing 2 - scrubbing comments."
    (is (= [[:mov :a 5]
            [:inc :a]
            [:call :foo]
            [:msg "(5+1)/2 = ;" :a]
            [:end]
            [(keyword "foo:")]
            [:div :a 2]
            [:ret]]
           (parse "; my first program
                   mov a 5
                   inc a     ; increment a
                   call foo
                   msg '(5+1)/2 = ;' a ; another comment.
                   end
                   foo:
                   div a 2
                   ret")))))

(deftest parser-with-long-register-names
  (is (= [[:mov :abc 5]
          [:inc :abc]]
         (parse "mov abc 5
                 inc abc"))))

(deftest msg-parser-tests
  (testing "'(5+1)/2 = ' a ' is greater than ' b"
    (is (= [:msg
            "(5+1)/2 = "
            :a
            " is greater than "
            :b] (parse-msg "msg '(5+1)/2 = ' a ' is greater than ' b"))))
  (testing "a msg with a comment at end of line and ; in the quote."
    (is (= [:msg
            "(5+1)/2 = "
            :a
            " is greater than "
            :b] (parse-msg "msg '(5+1)/2 = ' a ' is greater than ' b ; a comment")))))


(deftest scrubbing-comments-tests
  (is (= "inc a" (scrub-comments "inc a   ; some comment")))
  (testing "we don't scrub comments from msg fields"
    (is (= "msg '(5+1)/2 = ' a ; another comment." (scrub-comments "msg '(5+1)/2 = ' a ; another comment.")))))