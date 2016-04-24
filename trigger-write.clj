(require '[datomic.api :as d]
         '[clojure.java.shell :refer [sh]])

(defn log [s]
  (println "\u001B[32m" "=>" s "\u001B[0m"))

(defn random-str [n l]
  (take n
        (repeatedly (fn []
                      (reduce str
                              (take l
                                    (repeatedly
                                     #(rand-nth "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"))))))))

(def num-inserts 1000000)

;;(def db-uri "datomic:dev://localhost:4334/test")
(def base-uri "datomic:cass://localhost:9042/datomic.datomic")
(def db-uri (str base-uri "/test"))


(log "Allowing transactor to start up")
(Thread/sleep 2000)


(let [deleted (d/delete-database db-uri)]
  (log (str "deleted datbase" deleted)))

(d/create-database db-uri)

(def conn (d/connect db-uri))

(def datomic-distr (System/getenv "DATOMIC"))


(log "create console as second peer")
(future
  (let [res (sh  (str datomic-distr "/bin/console") "-p" "8080" "test" base-uri)]
    (log (:err res))
    (log (:out res))))

(def minimal-schema  [{:db/id #db/id[:db.part/db]
                        :db/ident :a/name
                        :db/valueType :db.type/string
                        :db/cardinality :db.cardinality/one
                        :db/doc "A name"
                        :db.install/_attribute :db.part/db}])

(log "installing schema")
@(d/transact conn minimal-schema)

(log (str  "adding " num-inserts " records"))

(try
  (doall (->> (random-str num-inserts 50)
                 (map #(assoc {} :db/id (d/tempid :db.part/user) :a/name %))
                 (partition 100)
                 (map #(d/transact conn (into [] %)))
                 (map deref)))
  (catch Exception e (log (.getMessage e))))


(def indexed [{:db/id :a/name
                  :db/index true
                  :db.alter/_attribute :db.part/db}])

(def unique [{:db/id :a/name
              :db/unique :db.unique/identity
              :db.alter/_attribute :db.part/db}])

(log "Allowing dust to settle down")
(Thread/sleep 1000)
(log "Appying uniqueness alteration")
(def with-index @(d/transact conn indexed))
@(d/sync-schema conn (d/basis-t (:db-after with-index)))
@(d/transact conn unique)

;;(d/release conn)


