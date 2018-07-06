(ns forridan.interceptor-test
  (:require [forridan.chain :as chain]
            [forridan.context :as c]
            [forridan.interceptor :as i]
            [clojure.spec.alpha :as s]
            [clojure.test :as t]))

;; set up a basic context
(def context {:coeffect/event [:bid/placed {:id 1 :user 10 :bid 3.14}]
              :coeffect/db {10 "Bob"}
              ::c/interceptor-key ::i/before
              ::c/queue []
              ::c/stack '()})

;; and define some specs
(s/def :coeffect/db map?)
(s/def :coeffect/event vector?)
(s/def :effect/tx vector?)

;; define a normal handler
;; 2 lines of business logic
;; 4 lines of (d|r)estructuring
(defn ugly-handler
  [{db :coeffect/db
    [event-name event-data] :coeffect/event
    :as context}]
  (let [{:keys [id user bid]} event-data
        name (get db user)]
    (assoc context :effect/tx [[id user name bid]])))

;; define a simple handler - unaware of context structure
;; much better, much easier to understand and test
(defn handler
  [db [event-name event-data]]
  (let [{:keys [id user bid]} event-data
        name (get db user)]
    [[id user name bid]]))

;; and a function spec for args and return value
(s/fdef handler
        :args (s/cat :db :coeffect/db :event :coeffect/event)
        :ret :effect/tx)

(comment
(i/magic-spec-call handler {:coeffect/event [:bid/placed {:id 1 :user 10 :bid 3.14}]
                            :coeffect/db {10 "Bob"}})
=>
{:coeffect/event [:bid/placed {:id 1, :user 10, :bid 3.14}], :coeffect/db {10 "Bob"}, :effect/tx [[1 10 "Bob" 3.14]]} 
)

(t/deftest magic-spec-call
  (let [result (i/magic-spec-call handler context)]
    (t/is (= result (ugly-handler context)))
    (t/is (= [[1 10 "Bob" 3.14]]
             (:effect/tx result)))
    (t/is (= context
             (dissoc result :effect/tx)))))

(def handler-interceptor
  {::i/name "handler"
   ::i/before (fn [c] (i/magic-spec-call handler c))})

(t/deftest magic-spec-call-test
  (let [context (update context ::c/queue conj handler-interceptor)
        result (chain/execute context)
        ;; ignore the machinery
        result (dissoc result ::c/interceptor-key ::c/queue ::c/stack)]
    (t/is (= result
             {:coeffect/event [:bid/placed {:id 1, :user 10, :bid 3.14}],
              :coeffect/db {10 "Bob"},
              :effect/tx [[1 10 "Bob" 3.14]]}))))
