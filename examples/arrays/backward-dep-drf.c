// -*- tab-width:2 ; indent-tabs-mode:nil -*-
//:: cases backward-dep-drf
//:: tools silicon

/*@
  invariant  a!=NULL && b !=NULL && c!=NULL;
  invariant \length(a)==N && \length(b)==N && \length(c)==N;

  requires (\forall* int i ; 0 <= i && i < N ; Perm(a[i],write));
  requires (\forall* int i ; 0 <= i && i < N ; Perm(b[i],1/2));
  requires (\forall* int i ; 0 <= i && i < N ; Perm(c[i],write));

  ensures  (\forall* int i ; 0 <= i && i < N ; Perm(a[i],write));
  ensures  (\forall* int i ; 0 <= i && i < N ; Perm(b[i],1/2));
  ensures  (\forall* int i ; 0 <= i && i < N ; Perm(c[i],write));
  @*/
void example(int a[],int b[],int c[],int N){  
  for(int i=0;i < N;i++)
   /*@
    requires Perm(a[i],1/2) ** Perm(b[i],1/2) ** Perm(c[i],write);
    requires (i==0 ==> Perm(a[i],1/2)) ** (i < N-1 ==> Perm(a[i+1],1/2));
    ensures  Perm(a[i],1/2) ** Perm(a[i],1/2) ** Perm(b[i],1/2) ** Perm(c[i],write);
   @*/
    {
    /*@
      S1:if (i>0) {
        recv Perm(a[i],1/2) from S2,1;
      }
    @*/
    a[i]=b[i]+1;
    S2:if (i < N-1) {
      c[i]=a[i+1]+2;
      //@ send Perm(a[i+1],1/2) to S1,1;
    }
  }
}
