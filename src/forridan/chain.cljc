(ns forridan.chain
  (:require [clojure.core.async :as a]
            [forridan.context :as c]
            [forridan.interceptor :as i]))

(def channel? (partial instance? clojure.core.async.impl.protocols.Channel))

(declare execute)

(defn step
  "Process the next interceptor in the chain.
  Pop the interceptor off the queue and execute it on the context."
  [{queue ::c/queue
    stack ::c/stack
    k ::c/interceptor-key
    :as context}]
  (let [interceptor (peek queue)
        new-queue (pop queue)
        new-stack (conj stack interceptor)
        f (k interceptor)]
    (if-not f
      context
      (let [new-context (-> context
                            (assoc ::c/queue new-queue
                                   ::c/stack new-stack)
                            f)]
        (if (channel? new-context)
          (a/go (execute (a/<! new-context)))
          new-context)))))


(defn execute
  "Process the entire interceptor chain"
  [context]
  (if (empty? (::c/queue context))
    context
    (recur (step context))))
