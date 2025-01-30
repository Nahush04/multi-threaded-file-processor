# **Multi-threaded File Processing System in Java**

## **Overview**

This project is a **high-performance, multi-threaded file processing system** built in **Java**. It processes large CSV files efficiently using **multi-threading, database transactions, and structured validation**. The system ensures **ACID compliance**, prevents partial failures, and handles errors gracefully.

## **Features**

✅ **Multi-threaded Processing:** Uses `ExecutorService` to process each `BH` block in a separate thread.\
✅ **ACID Compliance:** Transactions ensure data integrity, rolling back if any part of a block fails.\
✅ **Data Validation:** Each `BH` block is validated independently before insertion.\
✅ **Parallel Execution:** Multiple `BH` blocks are processed simultaneously, improving performance.\
✅ **File Handling:** Moves files to `success` or `error` folders based on processing results.

## **How It Works**

1. **File Listener** watches a directory for incoming CSV files.
2. **Validation Service** checks each `BH` block separately before inserting data and validates using the `BI` row.
3. **Listener Service** handles database insertions using PostgreSQL and ensures transactions.
4. **Move Service** moves processed files to `success` or `error` folders.
5. **Batch Processing Service** handles multi-threaded execution of each `BH` block.
6. **Multi-threading (**`ExecutorService`**)** assigns a thread to each `BH` block for parallel processing.

## **Installation & Setup**

### **1. Clone the Repository**

```bash
git clone https://github.com/Nahush04/multi-threaded-file-processor.git
cd multi-threaded-file-processor
```

### **2. Configure PostgreSQL Database**

- Ensure PostgreSQL is installed and running.
- Update database configurations in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/yourdatabase
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
```

### **3. Build & Run the Project**

- **Using Maven:**

```bash
mvn clean install
mvn exec:java
```

- **Using Java:**

```bash
javac -d bin src/main/java/com/example/*.java
java -cp bin com.example.FileProcessor
```

## **Usage**

1. Place CSV files in the **incoming folder** which will scan for new files every 20 seconds.
2. The system will automatically detect, validate, and process the file.
3. Successfully processed files move to the `success` folder.
4. If any block fails, the file moves to the `error` folder.

## **CSV File Structure**

* `BH` (Batch Header): Marks the start of a batch : (Batch No, batch date, location)
* `TH` (Transaction Header): Represents a transaction : (Tran Ref Nbr, Date, transaction amount)
* `TI` (Transaction Item): Represents individual items : (Item Nbr, description, amount)
* `BI` (Batch Summary): Confirms batch integrity : (Batch No, item count)

```sample csv
BH,1,01/27/2025,us
TH,22,01/27/2025,400
TI,121,earbuds,100
TI,122,mobile,300
BI,1,2
```

## **Error Handling**

- **Invalid File Format:** Moves file to `error` folder.
- **Validation Failure:** Affected `BH` block is skipped, but valid blocks are processed.
- **Database Error:** Rolls back the transaction if an issue occurs during insertion.

## **License**

This project is licensed under the MIT License.

## **Contributors**

- [Nahush](https://github.com/Nahush04)

---

### 🚀 **Feel free to contribute and improve the system!**

