// -*- tab-width:2 ; indent-tabs-mode:nil -*-
//:: cases TestFloat
//:: tools silicon
//:: verdict Pass

class TestFloat {

  public static void main(String args[]){
    float a,b,c,d;
    
    a=(float)1;
    b=(float)2;
    c=a+b;
    d=b+a;
    
    //@ assert c == d;
    
    /*@
      seq<float> s=seq<float>{};
      
      //assert \sum(s)==((float)0);
    */
  }

  /*@
    invariant a!=null ** (\forall* int i; 0 <= i && i < a.length;Perm(a[i],1/2));
  @*/
  public float add(float a[]){
    int k=0;
    float res=(float)0;
    //@ seq<float> s=seq<float>{};
    //@ loop_invariant 0 <= k && k <= a.length;
    //@ loop_invariant |s| == k;
    //@ loop_invariant \sum(s) == res;
    //@ loop_invariant (\forall int i ; 0 <= i && i < k ; s[i]==a[i]);
    while(k<a.length){
      //@ s=s+seq<float>{a[k]};
      res=res+a[k];
      k=k+1;
    }
    return res;
  }

}

