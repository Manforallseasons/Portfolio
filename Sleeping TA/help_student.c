/**
 * Simulate helping a student
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include "ta.h"

void help_student(int sleep_time) 
{
	printf("Helping a student for %d seconds\n", sleep_time);

	sleep(sleep_time);
}
