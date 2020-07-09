(ns asm.interpreter-tests
  (:require [clojure.test :refer :all]
            [asm.interpreter :refer :all]))

(deftest mov-tests
  (is (= {:a 5}       (mov {} :a 5)))
  (is (= {:a 5 :b 5}  (mov {:b 5} :a :b))))

(deftest interpret-tests
  (is (= [0 {:a 6, :b 5, :c 5}] (interpret [[:mov :a 5]
                                            [:inc :a]
                                            [:mov :b :a]
                                            [:dec :b]
                                            [:mul :a :b]
                                            [:mov :c :b]
                                            [:add :a :c]
                                            [:sub :a :b]
                                            [:div :a :c]
                                            [:end]
                                            [:inc :a]], true)))
  (testing "nops"
    (is (= [0 {:a 6, :b 5, :c 5}] (interpret [[:mov :a 5]
                                              [:inc :a]
                                              [:mov :b :a]
                                              [:dec :b]
                                              [:mul :a :b]
                                              [:mov :c :b]
                                              [:nop]
                                              [:nop]
                                              [:label :foo]
                                              [:add :a :c]
                                              [:sub :a :b]
                                              [:div :a :c]
                                              [:end]
                                              [:inc :a]], true))))
  (is (= [0 {:a 1}] (interpret [[:mov :a 0]
                                [:inc :a]
                                [:inc :a]
                                [:jmp :foo]
                                [:inc :a]
                                [:inc :a]
                                [:label :foo]
                                [:dec :a]
                                [:end]], true)))

  (is (= [0 {:a 0 :b 2 :c 1}] (interpret '([:mov :a 0]
                                           [:inc :a]
                                           [:inc :a]
                                           [:jmp :foo]
                                           [:inc :a]
                                           [:nop]
                                           [:nop]
                                           [:label :bar]
                                           [:mov :b 2]
                                           [:mul :a :b]
                                           [:jmp :quax]
                                           [:nop]
                                           [:nop]
                                           [:inc :a]
                                           [:label :foo]
                                           [:dec :a]
                                           [:jmp :bar]
                                           [:label :quax]
                                           [:mov :c 0]
                                           [:inc :c]
                                           [:dec :a]
                                           [:jnz :a -1]
                                           [:end]), true)))

  (testing "mul"
    (is (= [0 {:a 25}] (interpret [[:mov :a 5]
                                   [:mul :a 5]
                                   [:end]], true))))
    (testing "jne jumps"
    (is (= [0 {:a 5 :b 6}]
           (interpret [[:mov :a 5]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jne :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "jne does not jump"
    (is (= [0 {:a 36 :b 6}]
           (interpret [[:mov :a 6]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jne :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "je jumps"
    (is (= [0 {:a 30 :b 6}]
           (interpret [[:mov :a 5]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:je :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "je does not jump"
    (is (= [0 {:a 6 :b 6}]
           (interpret [[:mov :a 6]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:je :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "jge jumps on greater than"
    (is (= [0 {:a 7 :b 6}]
           (interpret [[:mov :a 7]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jge :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "jge jumps on equality"
    (is (= [0 {:a 7 :b 7}]
           (interpret [[:mov :a 7]
                       [:mov :b 7]
                       [:cmp :a :b]
                       [:jge :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "jge does not jump"
    (is (= [0 {:a 30 :b 6}]
           (interpret [[:mov :a 5]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jge :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "jg jumps"
    (is (= [0 {:a 7 :b 6}]
           (interpret [[:mov :a 7]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jg :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "jg does not jump"
    (is (= [0 {:a 36 :b 6}]
           (interpret [[:mov :a 6]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jg :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "jle jumps on equality"
    (is (= [0 {:a 6 :b 6}]
           (interpret [[:mov :a 6]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jle :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "jle jumps on less than"
    (is (= [0 {:a 5 :b 6}]
           (interpret [[:mov :a 5]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jle :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "jle does not jump"
    (is (= [0 {:a 6 :b 6}]
           (interpret [[:mov :a 6]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jle :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "jl jumps on less than"
    (is (= [0 {:a 5 :b 6}]
           (interpret [[:mov :a 5]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jl :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "jl does not jump"
    (is (= [0 {:a 36 :b 6}]
           (interpret [[:mov :a 6]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jl :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "jl does not jump on greater than"
    (is (= [0 {:a 42 :b 6}]
           (interpret [[:mov :a 7]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jl :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "call"
    (is (= [0 {:a 7}]
           (interpret [[:mov :a 7]
                       [:call :foo]
                       [:mul :a 7]
                       [:end]
                       [:label :foo]
                       [:end]], true))))

  (testing "call and ret"
    (is (= [0 {:a 56}]
           (interpret [[:mov :a 7]
                       [:call :foo]
                       [:mul :a 7]
                       [:end]
                       [:label :foo]
                       [:inc :a]
                       [:ret]], true))))
  (testing "complex 1"
    (is (= [0 {:a 7, :b 0, :c 3}]
           (interpret [[:mov :a 0]
                       [:mov :b 1]
                       [:mov :c 2]
                       [:call :foo]
                       [:mul :c :b]
                       [:cmp :a :b]
                       [:jne :quax]
                       [:mul :c 10]
                       [:label :quax]
                       [:nop]
                       [:call :bar]
                       [:xor :b :b]
                       [:end]
                       [:label :foo]
                       [:inc :b]
                       [:ret]
                       [:label :bar]
                       [:add :a 7]
                       [:sub :c 1]
                       [:ret]], true))))

  (testing "nested function calls"
    (= [0 {:a 1}] (interpret [[:call :foo]
                              [:inc :a]
                              [:end]
                              [:label :foo]
                              [:call :bar]
                              [:ret]
                              [:label :bar]
                              [:mov :a 0]
                              [:ret]], true))))

(deftest setting-return-values
  (is (= ["x = 5, y = 6" {:x 5 :y 6}]
         (interpret [[:mov :x 5]
                     [:mov :y 6]
                     [:msg "x = " :x ", y = " :y]
                     [:end]], true))))