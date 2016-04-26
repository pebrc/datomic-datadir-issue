(require '[datomic.api :as d])

(defn log [s]
  (println "\u001B[32m" "=>" s "\u001B[0m"))

(defn random-str [n l]
  (take n
        (repeatedly (fn []
                      (reduce str
                              (take l
                                    (repeatedly
                                     #(rand-nth "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"))))))))

(def num-inserts 100)

(def base-uri "datomic:dev://localhost:4334")
;;(def base-uri "datomic:cass://localhost:9042/datomic.datomic")
(def db-uri (str base-uri "/test"))


(log "Allowing transactor to start up")
(Thread/sleep 2000)


(let [deleted (d/delete-database db-uri)]
  (log (str "deleted datbase" deleted)))

(d/create-database db-uri)

(def conn (d/connect db-uri))

(def datomic-distr (System/getenv "DATOMIC"))


(def minimal-schema  [{:db/id #db/id[:db.part/db]
                        :db/ident :a/x
                        :db/valueType :db.type/string
                        :db/cardinality :db.cardinality/one
                        :db/doc "A x"
                       :db.install/_attribute :db.part/db}
                      {:db/id #db/id[:db.part/db]
                        :db/ident :a/y
                        :db/valueType :db.type/string
                        :db/cardinality :db.cardinality/one
                        :db/doc "A y"
                        :db.install/_attribute :db.part/db}])

(log "installing schema")
@(d/transact conn minimal-schema)

(log (str  "adding " num-inserts " records"))

(try
  (doall (->> (random-str num-inserts 50)
                 (map #(assoc {} :db/id (d/tempid :db.part/user) :a/x % :a/y %))
                 (partition 100)
                 (map #(d/transact conn (into [] %)))
                 (map deref)))
  (catch Exception e (log (.getMessage e))))


(def indexed { :db/index true
              :db.alter/_attribute :db.part/db})


(log "Allowing dust to settle down")
(Thread/sleep 1000)
(log "Appying indexed alteration")

(def attrs [{:db/id :a/x} {:db/id :a/y}])

(doall (map #(->> (merge indexed %)
                  ((fn [tx] (d/transact conn (conj  [] tx))))
                  (deref)) attrs))



