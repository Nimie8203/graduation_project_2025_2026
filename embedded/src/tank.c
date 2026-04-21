#include "tank.h"

void read_tank(void)
{
    if (g_state.pump_1_state && g_state.pump_2_state)
    {
        if (g_state.flow_sens_1 == 0 && g_state.flow_sens_2 == 0)
        {
            g_state.tank_state = false;
        }
        else
        {
            g_state.tank_state = true;
        }       
    }
    else
    {
        g_state.tank_state = true;
    }
}