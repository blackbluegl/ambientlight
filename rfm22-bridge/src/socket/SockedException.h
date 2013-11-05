/*
 * SockedException.h
 *
 *  Created on: 05.11.2013
 *      Author: florian
 */

#ifndef SOCKEDEXCEPTION_H_
#define SOCKEDEXCEPTION_H_

#include <exception>
#include <string>

using namespace std;

class SockedException : public std::exception{
public:
	SockedException(string m="exception!") : msg(m) {}
  ~SockedException() throw() {}
  const char* what() const throw() { return msg.c_str(); }

private:
  std::string msg;
};


#endif /* SOCKEDEXCEPTION_H_ */
