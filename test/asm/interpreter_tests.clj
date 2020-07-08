(ns asm.interpreter-tests
  (:require [clojure.test :refer :all]
            [asm.interpreter :refer :all]))

(deftest mov-tests
  (is (= {:a 5}       (mov {} :a 5)))
  (is (= {:a 5 :b 5}  (mov {:b 5} :a :b))))

(deftest interpret-tests
  (is (= {:a 6, :b 5, :c 5} (interpret [[:mov :a 5]
                                        [:inc :a]
                                        [:mov :b :a]
                                        [:dec :b]
                                        [:mul :a :b]
                                        [:mov :c :b]
                                        [:add :a :c]
                                        [:sub :a :b]
                                        [:div :a :c]
                                        [:end]
                                        [:inc :a]])))
  (testing "nops"
    (is (= {:a 6, :b 5, :c 5} (interpret [[:mov :a 5]
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
                                          [:inc :a]]))))
  (is (= {:a 1} (interpret [[:mov :a 0]
                            [:inc :a]
                            [:inc :a]
                            [:jmp :foo]
                            [:inc :a]
                            [:inc :a]
                            [:label :foo]
                            [:dec :a]
                            [:end]])))

  (is (= {:a 0 :b 2 :c 1} (interpret '([:mov :a 0]
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
                                       [:end]))))

  (testing "mul"
    (is (= {:a 25} (interpret [[:mov :a 5]
                               [:mul :a 5]
                               [:end]]))))
    (testing "jne jumps"
    (is (= {:a 5 :b 6 :internal-registers {:cmp :lt}}
           (interpret [[:mov :a 5]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jne :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "jne does not jump"
    (is (= {:a 36 :b 6 :internal-registers {:cmp :eq}}
           (interpret [[:mov :a 6]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jne :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "je jumps"
    (is (= {:a 30 :b 6 :internal-registers {:cmp :lt}}
           (interpret [[:mov :a 5]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:je :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "je does not jump"
    (is (= {:a 6 :b 6 :internal-registers {:cmp :eq}}
           (interpret [[:mov :a 6]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:je :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "jge jumps on greater than"
    (is (= {:a 7 :b 6 :internal-registers {:cmp :gt}}
           (interpret [[:mov :a 7]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jge :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "jge jumps on equality"
    (is (= {:a 7 :b 7 :internal-registers {:cmp :eq}}
           (interpret [[:mov :a 7]
                       [:mov :b 7]
                       [:cmp :a :b]
                       [:jge :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "jge does not jump"
    (is (= {:a 30 :b 6 :internal-registers {:cmp :lt}}
           (interpret [[:mov :a 5]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jge :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "jg jumps"
    (is (= {:a 7 :b 6 :internal-registers {:cmp :gt}}
           (interpret [[:mov :a 7]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jg :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "jg does not jump"
    (is (= {:a 36 :b 6 :internal-registers {:cmp :eq}}
           (interpret [[:mov :a 6]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jg :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "jle jumps on equality"
    (is (= {:a 6 :b 6 :internal-registers {:cmp :eq}}
           (interpret [[:mov :a 6]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jle :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "jle jumps on less than"
    (is (= {:a 5 :b 6 :internal-registers {:cmp :lt}}
           (interpret [[:mov :a 5]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jle :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "jle does not jump"
    (is (= {:a 6 :b 6 :internal-registers {:cmp :eq}}
           (interpret [[:mov :a 6]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jle :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "jl jumps on less than"
    (is (= {:a 5 :b 6 :internal-registers {:cmp :lt}}
           (interpret [[:mov :a 5]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jl :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "jl does not jump"
    (is (= {:a 36 :b 6 :internal-registers {:cmp :eq}}
           (interpret [[:mov :a 6]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jl :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "jl does not jump on greater than"
    (is (= {:a 42 :b 6 :internal-registers {:cmp :gt}}
           (interpret [[:mov :a 7]
                       [:mov :b 6]
                       [:cmp :a :b]
                       [:jl :foo]
                       [:mul :a :b]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "call"
    (is (= {:a 7}
           (interpret [[:mov :a 7]
                       [:call :foo]
                       [:mul :a 7]
                       [:end]
                       [:label :foo]
                       [:end]]))))

  (testing "call and ret"
    (is (= {:a 56}
           (interpret [[:mov :a 7]
                       [:call :foo]
                       [:mul :a 7]
                       [:end]
                       [:label :foo]
                       [:inc :a]
                       [:ret]])))))