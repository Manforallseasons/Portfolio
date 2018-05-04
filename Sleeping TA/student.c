/**
 * General structure of a student.
 *
 */

#include <pthread.h>
#include <stdio.h>
#include <time.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>
#include "ta.h"

void *student_loop(void *param)
{
	int number = *((int *)param);
	int sleep_time;

	printf("I am student %d\n", number);

	srandom((unsigned)time(NULL));
	sleep_time = (int)((random() % MAX_SLEEP_TIME) + 1);
	int waiting = 0;
		while(1)
		{
			if(waiting <  NUM_OF_SEATS){
				printf("TA is asleep.\n");
				printf("Student %d is waking up the TA.\n", number);
				pthread_mutex_lock(&mutex);
				waiting++;
				pthread_mutex_unlock(&mutex);

				sem_post(&sem_ta);
				printf("Student %d is waiting. %d seats taken.\n", number, waiting);

				sem_wait(&sem_s);
				pthread_mutex_lock(&mutex);
				waiting--;
				pthread_mutex_unlock(&mutex);
				printf("Student %d is being helped by the TA. \n", number);
			}
			else{
				programming(sleep_time);
			}
		}	
	return NULL;
}
