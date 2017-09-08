Required: What’s the easiest way for me to compile and test your code?
• Required: A description of what you could accomplish and what you learned.
    I think I safely got the speed data on both the old format and the csv format.
    I made small graphs within the terminal to help me with observing data behavior (does this look like speed)
    I also got power data on both formats. While I do get results that 'could' be correct, I think that there is a
    problem with my integration method which prevents me from definitely getting the correct number.
    I also implemented a costly time signature checking function (which is why oldFormatParser's second power
    calculation will take sometime), but still I got similar results.
• Required: The numerical results you got for all the parts you completed
    Speed:
    old format: Average front-0 mph= 26.34382623213217
                Minimum front-0 mph= 1.2594121017379762
                Maximum front-0 mph= 64.01725062817383
                Average front-1 mph= 27.7263342706777
                Minimum front-1 mph= 1.558881633544922
                Maximum front-1 mph= 135.5538884663086
    csv format: Average front-0 mph= 23.938807408831714
                Minimum front-0 mph= 6.193471E-37
                Maximum front-0 mph= 63.83601
                Average front-1 mph= 24.16746432183748
                Minimum front-1 mph= 2.34087E-38
                Maximum front-1 mph= 65.26362

                Average rear-0 mph = 24.93897091138407
                Minimum rear-0 mph= 3.824707E-38
                Maximum rear-0 mph= 67.53808
                Average rear-1 mph= 24.914515608382654
                Minimum rear-1 mph= 3.798172E-38
                Maximum rear-1 mph= 67.6329

    Power:
    old format: Total power kWh (first method)= 5.305708796957283

    csv format: Total Power (From V and I) kWh= 2.8373751913015064
                Total Power from node by node calculation kWh= 2.8063597084182432
• What was the hardest part?
    The hardest part was being completely unable to tell that I had mistaken diameter for circumference. It was
    aggravating. I'm used to the metric system so I couldn't easily visualize the sizes, which added to the error.
    That aside, I still cannot figure out why my integration isn't clean. I have some ideas, but not enough time to
    explore them.
• What are some of the pros and cons of each log format? Why?
    The csv format is much more readable and faster but it has more overhead.
    The old format is slower but more precise with higher precision at every data point (I think).
• How fast are your parser(s)? What could you do to improve performance? What’s the
time complexity of your algorithm?
    Without the greedy time signature security function, analyzing the bulk of the data is pretty fast. If I could have
    the data in a stream, it would be even faster. Though I could have written the code that way, I wanted a more
    top-down view of the data to have more control initially.
    To improve performance on a meta level, I can simply switch to a stream-based code. With the current system, I can
    further increase the speed of the time signature code by making it check specific windows of time.
    For simple speed logging (with average, minimum, maximums) the time complexity should be linear.
    For power analysis should be n^2.
    For time signature security, it's currently horrendous. It could be lowered to linear.
    To be fair, I don't know much about time complexity yet, I just have an intuition about it.
• Can you create a graph of some of the data? Feel free to use excel etc. You can graph
whatever you want, but I suggest looking at the front and rear wheel speeds from our
acceleration run. Do you notice anything interesting?
    I created a graph in the terminal, though I didn't export it for further visualisation.
• Why might front and rear wheel speeds produce different results?
• The data timestamps are created by the logging program. Is there anything wrong with
doing this? Do you notice any difference between the timestamps on the csv format and
the first format?
• Each piece of data has to be sent out separately, meaning that you could get two different
pieces of information you need at different times. How did you deal with this? Is there a
better way?