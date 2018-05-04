/**
 * General structure of the teaching assistant.
 *
 */

#include <pthread.h>
#include <stdio.h>
#include <time.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>
#include "ta.h"

void *ta_loop(void *param)
{
	printf("I am the TA\n");
	
	while(1)
	{
		printf("TA is Sleeping now.\n");

		sem_wait(&sem_ta);
		help_student(2);
		sem_post(&sem_s);
		help_student(2);
	}
	return NULL;
}
