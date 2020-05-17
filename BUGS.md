### **`1 : zero requests`**
The worker threads enter an infinite cycle of blank requests.

They are currently blocked from sending these requests to the Bank.

They make these repetitive blank requests more frequently in cases where they need fewer instances per resource

I think it is to do with the way java randoms are bound.

**_potential fix:_**

rewrite the request generation part of the run method in the worker class so that :
- if Need is a non-zero vector

- but request is a zero vector

- the program picks a non zero element in Need and  asks for 1 instance of the corresponding resource in the request
   

 