# To do
- survey: keyword, author, group
- design model

Keyword: tetris
Learning Tetris Using the Noisy Cross-Entropy Method (https://www.mitpressjournals.org/doi/pdf/10.1162/neco.2006.18.12.2936)
Tetris is Hard, Even to Approximate (https://link.springer.com/chapter/10.1007/3-540-45071-8_36)
How to lose at Tetris (http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.55.8562&rep=rep1&type=pdf)
An evolutionary approach to Tetris (https://www2.informatik.uni-erlangen.de/EN/publication/download/mic.pdf)
Improvements on Learning Tetris with Cross Entropy (https://hal.inria.fr/inria-00418930/document)
Approximate Dynamic Programming Finally Performs Well in the Game of Tetris (https://hal.inria.fr/hal-00921250/document)
On the evolution of artificial Tetris players (https://hal.archives-ouvertes.fr/hal-00397045v2/document)
Applying reinforcement learning to Tetris (http://www.cs.ru.ac.za/research/g02C0108/files/litreviewfinalhandin.pdf)
Reinforcement of Local Pattern Cases for Playing Tetris (http://www.aaai.org/Papers/FLAIRS/2008/FLAIRS08-066.pdf)
Learning to play Tetris applying reinforcement learning methods (https://pdfs.semanticscholar.org/e9f7/5616163593af943a26434ec0a139fef033ef.pdf)


# Scope of study / Goal
- each player do 1 step? (same piece per second) -> attack per piece / safe condition
- opp do N step, ai do 1 step
- as fast as possible

# Main focus
- Improve two player level rather than stack forever

# Method
- CNN on the field surface -> get good or bad case (precompute?) 
- reinforcement learn on state(field, cur, next, oppstate) [http://cs231n.stanford.edu/reports/2016/pdfs/121_Report.pdf]
- CNN whole state
- heuristic [https://codemyroad.wordpress.com/2013/04/14/tetris-ai-the-near-perfect-player/]
- el-tetris [http://imake.ninja/el-tetris-an-improvement-on-pierre-dellacheries-algorithm/]

# Representation of the field
- contour/surface
- top two level [http://www.math.tau.ac.il/~mansour/rl-course/student_proj/livnat/tetris.html]
- how to handle holes? and height?
