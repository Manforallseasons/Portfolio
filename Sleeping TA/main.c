/**
 * Comments go here.
 */

#include <pthread.h>
#include <stdio.h>
#include <semaphore.h>
#include <string.h>
#include <errno.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include "ta.h"

pthread_t ta;
pthread_t students[NUM_OF_STUDENTS];

/**
 * Initialize all relevant data structures and
 * synchronization objects.
 */
void init()
{
	int i;
	for (i = 0; i < NUM_OF_STUDENTS; i++)
		student_id[i] = i;
	pthread_mutex_init(&mutex, NULL);

	sem_init(&sem_s, 0,0);
	sem_init(&sem_ta, 0,0);
}

/**
 * Create the student threads.
 */
void create_students()
{
int i;

	for (i = 0; i < NUM_OF_STUDENTS; i++) {
		pthread_create(&students[i], 0, student_loop, (void *)&student_id[i]);
	}
}

/**
 * Create the TA thread.
 */
void create_ta()
{
	pthread_create(&ta, 0, ta_loop, 0);
}

int main(void)
{
int i;

	init();

	create_ta();

	create_students();

        for (i = 0; i < NUM_OF_STUDENTS; i++)
                pthread_join(students[i], NULL);

	pthread_join(ta, NULL);

	return 0;
}
