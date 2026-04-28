#include "pipe.h"

void read_pipe(void)
{
    if (g_state.pump_1_state && g_state.pump_2_state)
    {
        if (g_state.flow_sens_1 > 0 && g_state.flow_sens_2 == 0)
        {
            g_state.pipe_state = false;
        }
        else if (g_state.flow_sens_2 > 0 && g_state.flow_sens_1 == 0)
        {
            g_state.pipe_state = false;
        }
        else
        {
            g_state.pipe_state = true;
        }
    }
    else
    {
        g_state.pipe_state = true;
    }
}