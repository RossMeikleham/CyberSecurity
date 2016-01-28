
/*
non-empty zero-indexed array A consisting of N integers is given. The array contains an odd number of elements, and each element of the array can be paired with another element that has the same value, except for one element that is left unpaired.

For example, in array A such that:

  A[0] = 9  A[1] = 3  A[2] = 9
  A[3] = 3  A[4] = 9  A[5] = 7
  A[6] = 9
the elements at indexes 0 and 2 have value 9,
the elements at indexes 1 and 3 have value 3,
the elements at indexes 4 and 6 have value 9,
the element at index 5 has value 7 and is unpaired.
Write a function:

int solution(int A[], int N);

that, given an array A consisting of N integers fulfilling the above conditions, returns the value of the unpaired element.

For example, given array A such that:

  A[0] = 9  A[1] = 3  A[2] = 9
  A[3] = 3  A[4] = 9  A[5] = 7
  A[6] = 9
the function should return 7, as explained in the example above.

Assume that:

N is an odd integer within the range [1..1,000,000];
each element of array A is an integer within the range [1..1,000,000,000];
all but one of the values in A occur an even number of times.
Complexity:

expected worst-case time complexity is O(N);
expected worst-case space complexity is O(1), beyond input storage (not counting the storage required for input arguments).
Elements of input arrays can be modified.
*/



//Radix sort code copied from http://rosettacode.org/wiki/Sorting_algorithms/Radix_sort#C

#include <stdio.h>
#include <limits.h>
#include <stdlib.h>




/* sort signed ints > 0 */
static void radix_sort(int *from, int *to, int bit)
{
    if (!bit || to < from + 1) return;
 
    int *ll = from, *rr = to - 1, tmp;
    while (1) {
        /* find left most with bit, and right most without bit, swap */
        while (ll < rr && !(*ll & bit)) ll++;
        while (ll < rr &&  (*rr & bit)) rr--;
        if (ll >= rr) break;
      
        int temp = *ll;
        *ll = *rr;
        *rr = temp;
    }
 
    if (!(bit & *ll) && ll < to) ll++;
    bit >>= 1;
 
    radix_sort(from, ll, bit);
    radix_sort(ll, to, bit);
}
 


int solution(int A[], int N) {
    radix_sort(A, A + N, 1 << 30);
    
    for (int i = 0; i < N - 1; i+=2) {
        if (A[i] != A[i + 1]) {
            return A[i];           
        } 
    }
    return A[N - 1];
}


int main(int argc, char *argv[]) {
    int x[] = {9, 3, 9, 3, 9, 7, 9}; 

    for (int i = 0; i < sizeof(x) / sizeof(int); i++) {
        printf("%d ", x[i]);
    }
    printf("\n");

    printf("result: %d\n", solution(x, sizeof(x)/sizeof(int))); 
   
    for (int i = 0; i < sizeof(x)/sizeof(int); i++) {
        printf("%d ", x[i]);
        
    }

    printf("\n");

    return 0;
}

