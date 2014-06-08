:- module(exchange,[
		exchange/2
	]).
%swaps the element at POS (zero base) towards the right.
swaplist(L,POS,RETL):-swaplist(L,0,POS,RETL).
swaplist([X|[Y|RL]],POS,POS,[Y|[X|RL]]).
swaplist([X|RL],CNT,POS,[X|RETS]):-NCNT is CNT+1,swaplist(RL,NCNT,POS,RETS).

%reverses the number X
reverseNum(X,RX):-reverseNum(X,0,RX).
reverseNum(0,TRX,TRX).
reverseNum(X,TRX,RX):-MODX is X mod 10,DIVX is X//10,NTRX is (TRX*10)+MODX,reverseNum(DIVX,NTRX,RX).

%computes the base exchange of L
exchangeSumAction(L,RET):-exchangeSumActionPlus(L,RET).

exchangeSumActionPlus([X|L],RET):-exchangeSumActionMinus(L,RET2),RET is RET2+X.
exchangeSumActionPlus([],0).

exchangeSumActionMinus([X|L],RET):-exchangeSumActionPlus(L,RET2),RET is RET2-X.
exchangeSumActionMinus([],0).

%Creates a binary counter
createCounter(NUM,RETC):-NUM>=0,createCounter(NUM,0,RETC).
createCounter(NUM,NUM,[]).
createCounter(NUM,CNT,[0|RETC]):- NCNT is CNT+1,createCounter(NUM,NCNT,RETC).

%increments a binary counter. Returns false if it has reached max.
incrementCounter([0|L],[1|L]).
incrementCounter([1|L],[0|RL]):-incrementCounter(L,RL).

%all swaps happen towards the  right
%0 means that it will be swapped, 1 means that it won't be swapped
validPermutation(CL):-validPermutation(0,CL).
validPermutation(_,[1]).
validPermutation(_,[1|CL]):-validPermutation(0,CL).
validPermutation(0,[0|CL]):-validPermutation(1,CL).

%0 means that it will be swapped, 1 means that it won't be swapped
%remember to reverse the number when swapping!!!
%valid permutation ensure that there won't be any illegal swaps.
applyPermutation([],[],[]).
applyPermutation([X|L],[1|CL],[X|RETL]):-applyPermutation(L,CL,RETL).
applyPermutation([X|[Y|L]],[0|[1|CL]],[RY|[RX|RETL]]):-reverseNum(X,RX),reverseNum(Y,RY),applyPermutation(L,CL,RETL).

testPermutations(L,CL,RET):-( %If this is a normal permutation
								(incrementCounter(CL,NCL),testPermutations(L,NCL,RET1),
								( %If this is a valid permutation
									(validPermutation(CL),applyPermutation(L,CL,NL),
									exchangeSumAction(NL,THISRET),RET is max(RET1,THISRET))
								; %Else
									(RET is RET1)
								))
							; %Else if this is the final permutation [1,1 ... 1]
								(exchangeSumAction(L,RET))
							).

exchange(L,RET):-length(L,LENL),createCounter(LENL,RETC),testPermutations(L,RETC,RET).