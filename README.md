# Scope of study / Goal
- each player do 1 step? (same piece per second)
- as fast as possible

# Main focus
- Improve two player level rather than stack forever

# Method
- CNN on the field surface -> get good or bad case (precompute?) 
- reinforcement learn on state(field, cur, next, oppstate) [http://cs231n.stanford.edu/reports/2016/pdfs/121_Report.pdf]
- CNN whole state (
- heuristic [https://codemyroad.wordpress.com/2013/04/14/tetris-ai-the-near-perfect-player/]

# Representation of the field
- contour/surface
- top two level [http://www.math.tau.ac.il/~mansour/rl-course/student_proj/livnat/tetris.html]
- how to handle holes? and height?
